package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 11:18
 */
public interface ActivityService {

    boolean pushRexdisStock();

    List<SeckillGoods> findAll();

    SeckillGoods getSeckillGoods(Long skuId);

    boolean checkSkuIdStr(String skuIdStr);

    void seckillOrder(Long skuId, String userId);

    Result checkOrder(String userId, String skuId);

    boolean deleteRedisSeckill();
}
