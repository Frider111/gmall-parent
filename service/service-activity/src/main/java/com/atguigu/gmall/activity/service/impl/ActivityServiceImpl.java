package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.ActivityMapper;
import com.atguigu.gmall.activity.service.ActivityService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 11:18
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    ActivityMapper activityMapper;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 把库存数据推送到 redis 当中
     * 把列表放到 hash 当中 sku 对应 商品详情
     * 把数据放在 list 集合当中
     *
     * @return
     */
    @Override
    public boolean pushRexdisStock() {

        QueryWrapper<SeckillGoods> sqlQueryWrapper = new QueryWrapper<SeckillGoods>();

        sqlQueryWrapper.eq("status", 1);
        sqlQueryWrapper.gt("num", 0);
        sqlQueryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));

        List<SeckillGoods> seckillGoods = activityMapper.selectList(sqlQueryWrapper);

        for (SeckillGoods seckillGood : seckillGoods) {
            // 把物品添加到 redis hash 当中
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGood.getSkuId() + "", seckillGood);

            for (int i = 0; i < seckillGood.getNum(); i++) {

                redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + seckillGood.getSkuId()).leftPush(seckillGood);

            }
            // redis 发送频道 1 代表发送 0 代表没库存了
            redisTemplate.convertAndSend("seckillpush", seckillGood.getSkuId() + ":1");
        }
        return true;
    }

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckils = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();

        return seckils;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {


       SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");

        return seckillGoods;
    }

    @Override
    public boolean checkSkuIdStr(String skuIdStr) {
        Object o = redisTemplate.opsForValue().get(skuIdStr);
        if (o == null) {
            return false;
        }
        return true;
    }

    @Override
    public void seckillOrder(Long skuId, String userId) {

        // setIfAbsent 如果 redis 没有这个数据，就添加，有的话返回false
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId,
                RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
//         表示有的话，返回提醒他添加过了
        if (!isExist)
        {
            return;
        }

        String state = (String) CacheHelper.get(skuId + "");

        if ("0".equals(state)) {
            return;
        }

        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX+skuId).rightPop();

        if (seckillGoods != null) {
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setUserId(userId);
            orderRecode.setSeckillGoods(seckillGoods);
            orderRecode.setNum(1);
            orderRecode.setOrderStr(MD5.encrypt(userId + skuId));

            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userId, orderRecode);
            updateStockCount(skuId+"");
        } else {
            redisTemplate.convertAndSend("seckillpush", skuId + ":0");
            return;
        }
    }

    /**
     * 做完回来再写
     * @param userId
     * @param skuId
     * @return
     */
    @Override
    public Result checkOrder(String userId, String skuId) {

        // 用户在缓存中存在，有机会秒杀到商品 , 不存在就是在排队。
        boolean isUser =redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if (isUser)
        {
            boolean isPreOrder = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if (isPreOrder)
            {
                return Result.build(null, ResultCodeEnum.SECKILL_SUCCESS) ;
            }
            else
            {
                boolean aBoolean = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId) ;
                if (aBoolean)
                {
                    return Result.build(null, ResultCodeEnum.SECKILL_ORDER_SUCCESS) ;
                }
                return Result.build(null, ResultCodeEnum.SECKILL_FINISH) ;
            }
        }
        else {
            Object o = CacheHelper.get(skuId);
            if ("0".equals(o))
            {
                return Result.build(null,ResultCodeEnum.SECKILL_FINISH) ;
            }
            return Result.build(null, ResultCodeEnum.SECKILL_RUN) ;
        }
    }

    @Override
    public boolean deleteRedisSeckill() {

        QueryWrapper<SeckillGoods> sqlQueryWrapper = new QueryWrapper<SeckillGoods>();

        sqlQueryWrapper.eq("status", 1);
        sqlQueryWrapper.eq("DATE_FORMAT(end_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));

        List<SeckillGoods> seckillGoods1 = activityMapper.selectList(sqlQueryWrapper);

        for (SeckillGoods seckillGoods : seckillGoods1) {
            redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId());
        }
        redisTemplate.delete(RedisConst.SECKILL_GOODS);
        redisTemplate.delete(RedisConst.SECKILL_ORDERS);
        redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
//将状态更新为结束
        SeckillGoods seckillGoodsUp = new SeckillGoods();
        seckillGoodsUp.setStatus("2");
        activityMapper.update(seckillGoodsUp, sqlQueryWrapper);
        return false;
    }

    public void updateStockCount(String skuId) {
        Long stockCount = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        // 批量修改数据
        QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
        seckillGoodsQueryWrapper.eq("sku_id", skuId);
        SeckillGoods seckillGoods = activityMapper.selectOne(seckillGoodsQueryWrapper);

        if (stockCount % 2 == 0) {

            seckillGoods.setNum(stockCount.intValue());
            activityMapper.updateById(seckillGoods);
            //更新缓存
            redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId, seckillGoods);

        }
    }

}
