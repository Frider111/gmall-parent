package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.common.time.GmallChangeTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 13:53
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient ;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    ListFeignClient listFeignClient ;

    @GmallChangeTime
    @Override
    public Map<String, Object> getBySkuId(Long skuId) throws Exception {
//        return getBySkuId1(skuId);
        return getBySkuId2(skuId);
    }


    public Map<String, Object> getBySkuId1(Long skuId) throws Exception {

        Map<String, Object> result = new HashMap<>();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        BigDecimal price = productFeignClient.getSkuPrice(skuId);

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = productFeignClient.getSkuSaleAttrValueListBySpu(skuInfo.getId(),
                skuInfo.getSpuId());
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(),
                skuInfo.getSpuId());

        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

        String valuesSkuJson = JSONObject.toJSONString(skuValueIdsMap);

        result.put("skuInfo",skuInfo);
        result.put("categoryView",categoryView);
        result.put("price",price);
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        result.put("valuesSkuJson",valuesSkuJson);

//        result.put();
        return result;
    }

    public Map<String, Object> getBySkuId2(Long skuId) throws Exception {


        Map<String, Object> result = new HashMap<>();

        CompletableFuture<SkuInfo> completableFutureSkuInfo = CompletableFuture.supplyAsync( () -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            result.put("skuInfo",skuInfo);
            return skuInfo;
        },threadPoolExecutor);

        CompletableFuture completableFuturePrice = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            result.put("price",price);
        },threadPoolExecutor);

        CompletableFuture<Void> completableFutureCategory = completableFutureSkuInfo.thenAcceptAsync((skuInfo) -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView",categoryView);
        },threadPoolExecutor);

        CompletableFuture<Void> completableFutureSpuSaleAttrs = completableFutureSkuInfo.thenAcceptAsync((skuInfo) -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = null;
            try {
                spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(),
                        skuInfo.getSpuId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        },threadPoolExecutor);

        CompletableFuture<Void> completableFutureSKuValueIdsMap = completableFutureSkuInfo.thenAcceptAsync((skuInfo) -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valuesSkuJson = JSONObject.toJSONString(skuValueIdsMap);
            result.put("valuesSkuJson",valuesSkuJson);
        },threadPoolExecutor);
        // 更新热点数据
        CompletableFuture<Void> completableHotsocre = CompletableFuture.runAsync(() -> {
                listFeignClient.hotScore(skuId);
        }, threadPoolExecutor);

        CompletableFuture.allOf(completableFutureSkuInfo,completableFuturePrice,completableFutureCategory,
                completableFutureSpuSaleAttrs,completableFutureSKuValueIdsMap).join();
        return result;
    }


}
