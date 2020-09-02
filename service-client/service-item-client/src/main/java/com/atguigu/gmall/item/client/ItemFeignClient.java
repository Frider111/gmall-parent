package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.product.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 14:07
 */
@FeignClient(value = "SERVICE-ITEM",fallback = ItemFeignClientFallback.class)
public interface ItemFeignClient {

    @GetMapping("/api/item/{skuId}")
    Result getItem(@PathVariable("skuId") Long skuId);

}
