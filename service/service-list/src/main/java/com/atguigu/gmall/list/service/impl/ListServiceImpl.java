package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.mapper.GoodsElasticsearch;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.inject.Scope;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 21:05
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    GoodsElasticsearch goodsElasticsearch;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 下架sku的时候对应删除es里面的数据
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        goodsElasticsearch.deleteById(skuId);
    }

    /**
     * 上架sku 的时候对应添加es的数据
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        // 初始化一个商品对象，维护在es当中
        Goods goods = new Goods();
        // 获取skuinfo数据
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 获取一二三及分类数据
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // 获取品牌数据
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        // 获取平台数据集合【根据skuid获取】
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        // 获取价格信息
        BigDecimal price = productFeignClient.getSkuPrice(skuId);

        // 封装品牌信息
        if (trademark != null) {
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }
        // 封装分类信息
        if (categoryView != null) {
            BeanUtils.copyProperties(categoryView, goods);
        }
        // 封装SearchAttr数据
        if (attrList != null) {
            List<SearchAttr> attrs = attrList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(attrs);
        }
        // 设置价格数据
        if (price != null) {
            goods.setPrice(price.doubleValue());
        }
        // 封装skuinfo 数据
        goods.setId(skuId);
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        goodsElasticsearch.save(goods);
    }

    /**
     * 热点数据 添加。
     * 第一步，先带 redis 添加数据
     * 第二步：设置一个阈值，达到阈值更新到es中 【阈值暂时用 10 】
     * redis 键值设置：hotscore zset 的 key value值设置为 skuid:#{skuid}
     *
     * @param skuId
     */
    @Override
    public void hotScore(Long skuId) {

        String zkey = "hotscore";
        // 获取 redis 数据
        Long hotscore = redisTemplate.opsForZSet().incrementScore(zkey, "skuid" + skuId, 1).longValue();
        if (hotscore % 10 == 0) {
            // 从 es 中获取数据
            Goods goods = goodsElasticsearch.findById(skuId).get();
            goods.setHotScore(hotscore);
            // 添加数据
            goodsElasticsearch.save(goods);
        }
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) throws IOException {

        SearchRequest searchRequest = buildQueryDsl(searchParam);
        // 执行搜索查询 ， 并且返回响应对象
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 获取响应的hit数据
        SearchResponseVo searchResponseVo = parseSearchResult(searchResponse);
        // 返回响应对象

        // 设置分页数据信息
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setTotalPages(searchResponseVo.getTotal()/searchResponseVo.getPageSize()+1);


        return searchResponseVo;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam) {

        // 获取分类id错误
        Long category1Id = searchParam.getCategory1Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id();
        // 获取关键字搜索数据
        String keyword = searchParam.getKeyword();

        String[] props = searchParam.getProps();

        String trademark = searchParam.getTrademark();

        String order = searchParam.getOrder();
        // 获取分页参数，设置es查询数据条数
        Integer pageSize = searchParam.getPageSize();
        Integer pageNo = searchParam.getPageNo();


        // 搜索代理对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置的查询库
        SearchRequest searchRequest = new SearchRequest("goods");
        // 设置的查询表
        searchRequest.types("info");
        // bool 查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


        // 根据分类数据到es中
        if (category1Id != null) {
            // 布尔下的子查询 【过滤条件】
            boolQueryBuilder.filter(new TermQueryBuilder("category1Id", category1Id + ""));
        } else if (category2Id != null) {
            boolQueryBuilder.filter(new TermQueryBuilder("category2Id", category2Id + ""));
        } else if (category3Id != null) {
            boolQueryBuilder.filter(new TermQueryBuilder("category3Id", category3Id + ""));
        }

        // 封装 关键字数据到es中
        if (StringUtils.isNotBlank(keyword)) {

            boolQueryBuilder.must(new MatchQueryBuilder("title", keyword));
            // 设置关键字高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red;weight:bold'>");
            highlightBuilder.postTags("</span>");
            HighlightBuilder highlightBuilder1 = highlightBuilder.field("title");
            searchSourceBuilder.highlighter(highlightBuilder1);

        }

        // 根据属性查询数据信息
        if (props != null) {

            for (String prop : props) {
                BoolQueryBuilder attrboolQueryBuilder1 = QueryBuilders.boolQuery();
                String[] propssplit = prop.split(":");

                attrboolQueryBuilder1.must().add(new TermQueryBuilder("attrs.attrId", propssplit[0]));
                attrboolQueryBuilder1.must().add(new TermQueryBuilder("attrs.attrValue", propssplit[1]));
                attrboolQueryBuilder1.must().add(new TermQueryBuilder("attrs.attrName", propssplit[2]));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", attrboolQueryBuilder1, ScoreMode.None);
                boolQueryBuilder.must(nestedQueryBuilder);
            }
        }

        // 根据品牌查询数据
        if (StringUtils.isNotBlank(trademark)){
            String[] tmstr = trademark.split(":");
            boolQueryBuilder.must(new MatchQueryBuilder("tmId", tmstr[0]));
        }

        // 把bool数据查询封装到 searchSourceBuilder 代理中

        // 封装品牌数据搞一波


        // 聚合品牌数据
        TermsAggregationBuilder tmidAgg = new TermsAggregationBuilder("tmIdAgg", ValueType.LONG).field("tmId");
        // 根据品牌 id 获取品牌名字 跟 品牌 url 数据
        TermsAggregationBuilder tmNameAgg = new TermsAggregationBuilder("tmNameAgg", ValueType.STRING).field("tmName");
        TermsAggregationBuilder tmLogoUrlAgg = new TermsAggregationBuilder("tmLogoUrlAgg", ValueType.STRING).field("tmLogoUrl");
        tmidAgg.subAggregation(tmNameAgg).subAggregation(tmLogoUrlAgg);
        // 将聚合封装到 搜索类
        searchSourceBuilder.aggregation(tmidAgg);

        // 聚合属性代码到es中
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))));
        // 排序查询数据
        if (order!=null){
            String[] split = order.split(":");
            String type = split[0];
            String field = "" ;
            switch (type){
                case "1":
                    field = "hotScore" ;
                    break;
                case "2":
                    field = "price" ;
                    break;
                default:
            }
            searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
        }
        // 设置分页数据
        searchSourceBuilder.size(pageSize);
//        searchSourceBuilder.size(4);
        searchSourceBuilder.from((pageNo-1)*pageSize);
        // 封装bool 查询到query查询
        searchSourceBuilder.query(boolQueryBuilder);
        // 输出es中对应查询语句
        System.out.println("searchSourceBuilder = " + searchSourceBuilder);

        // 把搜索对象
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }


    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {

        // 设置返回数据
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 获取相应数据
        SearchHits hits = searchResponse.getHits();
        // 封装结果集
        for (SearchHit hit : hits) {

            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            // 获取高亮数据
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null && !highlightFields.isEmpty()) {
                HighlightField title = highlightFields.get("title");
                // 高亮title修改good属性
                goods.setTitle(title.getFragments()[0].string());
            }

            searchResponseVo.getGoodsList().add(goods);
        }
//        Set<SearchResponseTmVo> searchResponseTmVos = new HashSet<>();
//        for (Goods goods : searchResponseVo.getGoodsList()) {
//            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
//            searchResponseTmVo.setTmId(goods.getTmId());
//            searchResponseTmVo.setTmName(goods.getTmName());
//            searchResponseTmVo.setTmLogoUrl(goods.getTmLogoUrl());
//            searchResponseTmVos.add(searchResponseTmVo) ;
//        }

        // 品牌聚合查询数据
        Aggregation tmIdAgg1 = searchResponse.getAggregations().get("tmIdAgg");
        ParsedLongTerms parsedLongTerms = (ParsedLongTerms) tmIdAgg1;
        List<SearchResponseTmVo> searchResponseTmVos = parsedLongTerms.getBuckets().stream().map((bucket) -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            searchResponseTmVo.setTmId(bucket.getKeyAsNumber().longValue());
            ParsedStringTerms parsedStringTerms = (ParsedStringTerms) bucket.getAggregations().get("tmNameAgg");
            String tmName = parsedStringTerms.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            ParsedStringTerms parsedStringTerms1 = (ParsedStringTerms) bucket.getAggregations().get("tmLogoUrlAgg");
            String tmUrl = parsedStringTerms1.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        // 品牌数据封装到返回类
        searchResponseVo.getTrademarkList().addAll(searchResponseTmVos);
        // 属性聚合查询数据
        ParsedNested parsedNested = (ParsedNested) searchResponse.getAggregations().get("attrAgg");

        ParsedLongTerms attrIdAgg = parsedNested.getAggregations().get("attrIdAgg");

        List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map((bucketAttrId) -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

            searchResponseAttrVo.setAttrId(bucketAttrId.getKeyAsNumber().longValue());

            ParsedStringTerms attrNameAggTrems = (ParsedStringTerms) bucketAttrId.getAggregations().get("attrNameAgg");
            String attrName = attrNameAggTrems.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            ParsedStringTerms attrValueAggTrems = (ParsedStringTerms) bucketAttrId.getAggregations().get("attrValueAgg");

            List<String> attrValues = attrValueAggTrems.getBuckets().stream().map((bucket) -> {
                String attrvalue = bucket.getKeyAsString();
                return attrvalue;
            }).collect(Collectors.toList());

            searchResponseAttrVo.setAttrValueList(attrValues);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        // 品牌数据封装到返回类
        searchResponseVo.setAttrsList(searchResponseAttrVos);

        // 获取到查询的总记录数
        long totalHits = hits.getTotalHits();
        searchResponseVo.setTotal(totalHits);


        return searchResponseVo;
    }
}
