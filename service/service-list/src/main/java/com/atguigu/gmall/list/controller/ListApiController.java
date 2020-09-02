package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.product.common.result.Result;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 21:01
 */
@RequestMapping("api/list")
@RestController
public class ListApiController {

    @Autowired
    ListService listService ;

    /**
     * 下架sku
     * @param skuId
     */
    @RequestMapping("cancelSale/{skuId}")
    public void cancelSale(@PathVariable("skuId") Long skuId){

        listService.cancelSale(skuId);
    }

    /**
     * 上架sku
     * @param skuId
     */
    @RequestMapping("onSale/{skuId}")
    public void onSale(@PathVariable("skuId") Long skuId){

        listService.onSale(skuId);
    }

    @RequestMapping("hotScore/{skuId}")
    public void hotScore(@PathVariable("skuId") Long skuId){


        listService.hotScore(skuId);
    }

    @PostMapping("list")
    public Result list(@RequestBody SearchParam searchParam) throws IOException {

        SearchResponseVo searchResonse = listService.list(searchParam);

        return Result.ok(searchResonse) ;
    }

}
