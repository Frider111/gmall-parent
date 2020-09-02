package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.product.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 17:11
 */
@Component
public class ItemFeignClientFallback implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
