package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.common.result.Result;
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
    @RequestMapping("inner/cancelSale/{skuId}")
    public void cancelSale(@PathVariable("skuId") Long skuId){

        listService.cancelSale(skuId);
    }

    /**
     * 上架sku
     * @param skuId
     */
    @RequestMapping("inner/onSale/{skuId}")
    public void onSale(@PathVariable("skuId") Long skuId){

        listService.onSale(skuId);
    }

    @RequestMapping("inner/hotScore/{skuId}")
    public void hotScore(@PathVariable("skuId") Long skuId){


        listService.hotScore(skuId);
    }

    @PostMapping("inner/list")
    public Result<SearchResponseVo> list(@RequestBody SearchParam searchParam) throws IOException {

        SearchResponseVo searchResonse = listService.list(searchParam);
        Result result = Result.ok(searchResonse) ;
        return result ;
    }

}
