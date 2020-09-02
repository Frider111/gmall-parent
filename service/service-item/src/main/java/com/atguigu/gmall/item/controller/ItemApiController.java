package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.product.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 14:05
 */
@RestController
@RequestMapping("/api/item/")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}")
    public Result getItem(@PathVariable("skuId") Long skuId) throws Exception {

        Map<String, Object> skuMap = itemService.getBySkuId(skuId);

        return Result.ok(skuMap);

    }

}
