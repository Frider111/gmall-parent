package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.product.common.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/8/31 - 16:08
 */
@Controller
@RequestMapping
public class ListController {

    @Autowired
    ListFeignClient listFeignClient ;

    @GetMapping({"list.html","search.html"})
    public String list(SearchParam searchParam, Model model) throws IOException {

        Map<String, String> orderMap = getOrderMap(searchParam);
        Result<Map> result = listFeignClient.list(searchParam);
        model.addAllAttributes(result.getData());
        String urlParam = getUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("orderMap", orderMap);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("trademarkParam", getTrademarkParam(searchParam));
        model.addAttribute("propsParamList", getPropsParamList(searchParam));
        return "list/index" ;

    }

    /**
     * 封装品牌数据
     * @param searchParam
     * @return
     */
    public String getTrademarkParam(SearchParam searchParam){
        String trademark = searchParam.getTrademark();
        String trademarkParam = "" ;
        if (StringUtils.isNotBlank(trademark))
        {
            String[] split = trademark.split(":");
            trademarkParam = split[1] ;
        }
        return trademarkParam ;
    }

    /**
     * 封装 prop数据
     * @param searchParam
     * @return
     */
    public List<SearchAttr> getPropsParamList(SearchParam searchParam){

        List<SearchAttr> searchAttrs = new ArrayList<>(10) ;
        String[] props = searchParam.getProps();

        if (props!=null)
        for (String prop : props) {
            SearchAttr searchAttr = new SearchAttr();
            String[] split = prop.split(":");
            searchAttr.setAttrId(Long.valueOf(split[0]));
            searchAttr.setAttrValue(split[1]);
            searchAttr.setAttrName(split[2]);
            searchAttrs.add(searchAttr);
        }

        return searchAttrs ;
    }


    /**
     * 处理返回给前端的排序数据
     * @param searchParam
     * @return
     */
    private Map<String,String> getOrderMap(SearchParam searchParam){
        Map<String,String> orderMap = new HashMap<>();
        if (StringUtils.isNotBlank(searchParam.getOrder()))
        {
            String[] split = searchParam.getOrder().split(":");
            orderMap.put("type", split[0]) ;
            orderMap.put("sort", split[1]) ;
        }
        else {
            orderMap.put("type", "1") ;
            orderMap.put("sort", "desc") ;
            searchParam.setOrder("1:desc");
        }
        return orderMap ;
    }

    /**
     * 返回一个urlParam路径
     * @param searchParam
     * @return
     */
    private String getUrlParam(SearchParam searchParam){

//        list.html?category2Id=13

        String urlParam = "" ;
        Long category1Id = searchParam.getCategory1Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();



        if (category1Id!=null){
            urlParam = "category1Id=" + category1Id.toString() ;
        }
        else if (category2Id!=null){
            urlParam = "category2Id=" + category2Id.toString() ;
        }
        else if (category3Id!=null){
            urlParam = "category3Id=" + category3Id.toString() ;
        }

        if (StringUtils.isNotBlank(keyword)){
            urlParam = "keyword=" + keyword ;
        }

        if (StringUtils.isNotBlank(trademark)){
            urlParam += "&trademark=" + trademark ;
        }

        if (props != null && props.length != 0) {
            for (String prop : props) {
                urlParam += "&props=" + prop ;
            }
        }

        return "list.html?" + urlParam ;
    }


}
