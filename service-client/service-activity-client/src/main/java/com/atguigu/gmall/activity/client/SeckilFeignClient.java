package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 14:24
 */
@FeignClient("SERVICE-ACTIVITY")
public interface SeckilFeignClient {

    @GetMapping("/api/activity/seckill/findAll")
    Result findAll();


    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable("skuId") Long skuId) ;

    @GetMapping("/api/activity/seckill/checkSkuIdStr/{skuIdStr}")
    boolean checkSkuIdStr(@PathVariable("skuIdStr") String skuIdStr);

    /**
     * 秒杀确认订单
     * @return
     */
    @GetMapping("/api/activity/seckill/auth/trade")
    Result<Map<String, Object>> trade();

}
