package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.ActivityService;
import com.atguigu.gmall.activity.service.impl.ActivityServiceImpl;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.const1.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 14:27
 */
@RequestMapping("api/activity/seckill")
@RestController
public class SeckilApiController {

    @Autowired
    ActivityServiceImpl activityService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService ;

    @Autowired
    UserFeignClient userFeignClient ;

    @Autowired
    OrderFeignClient orderFeignClient ;

    @GetMapping("findAll")
    public Result findAll(){

        List<SeckillGoods> seckils = activityService.findAll();
        return Result.ok(seckils);
    }


    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable("skuId") Long skuId)
    {
        SeckillGoods seckillGoods = activityService.getSeckillGoods(skuId);
        return Result.ok(seckillGoods);
    }

///auth/getSeckillSkuIdStr/' + skuId

    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") String skuId, HttpServletRequest request)
    {

        SeckillGoods seckillGoods = activityService.getSeckillGoods(Long.valueOf(skuId));

        if (seckillGoods!=null)
        {
            Date curTime = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), curTime) && DateUtil.dateCompare(curTime, seckillGoods.getEndTime()))
            {
                String userId = request.getHeader("userId");
                String userStr = MD5.encrypt(userId+skuId);
                redisTemplate.opsForValue().set(userStr, true,30, TimeUnit.MINUTES);
                return Result.ok(userStr);
            }
            return Result.fail();
        }
        return Result.fail();
    }

    /**
     * 检查是否有作弊的情况
     * @param skuIdStr
     * @return
     */
    @GetMapping("checkSkuIdStr/{skuIdStr}")
    public boolean checkSkuIdStr(@PathVariable("skuIdStr") String skuIdStr){

        return activityService.checkSkuIdStr(skuIdStr);

    }

//    '/auth/seckillOrder/' + skuId + '?skuIdStr=' + skuIdStr

    /**
     *    判断是否可以秒杀
     * 1，校验下单码，只有正确获得下单码的请求才是合法请求
     * 2，校验状态位state，
     * @param skuId
     * @param skuIdStr
     * @param request
     * @return
     */
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") String skuId,String skuIdStr,HttpServletRequest request)
    {
        String userId = request.getHeader("userId");

        if (!skuIdStr.equals(MD5.encrypt(userId+skuId)))
        {
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL) ;
        }
        String state = (String)CacheHelper.get(skuId);

        if (StringUtils.isBlank(state))
        {
            return Result.build(null, ResultCodeEnum.SECKILL_NO_START) ;
        }

        if ("0".equals(state))
        {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH) ;
        }

        UserRecode messageRecodeSeckill = new UserRecode();

        messageRecodeSeckill.setSkuId(Long.valueOf(skuId));
        messageRecodeSeckill.setUserId(userId);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,messageRecodeSeckill);

        return Result.ok() ;
    }

//    url: this.api_name + '/auth/checkOrder/' + skuId,
//    method: 'get'

    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") String skuId,HttpServletRequest request)
    {
        String userId = request.getHeader("userId");
        return activityService.checkOrder(userId,skuId);
    }


    /**
     * 秒杀确认订单
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request) {
// 获取到用户Id
        String userId = request.getHeader("userId");

// 先得到用户想要购买的商品！
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if (null == orderRecode) {
            return Result.fail().message("非法操作");
        }
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

//获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

// 声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
// 添加到集合
        detailArrayList.add(orderDetail);

// 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
// 保存总金额
        result.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(result);
    }

//    url: this.api_name + '/auth/submitOrder',
//    method: 'post',
    /**
     * 秒杀提交订单
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);

        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if (null == orderRecode) {
            return Result.fail().message("非法操作");
        }

        orderInfo.setUserId(Long.parseLong(userId));

        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (null == orderId) {
            return Result.fail().message("下单失败，请重新操作");
        }

        //删除下单信息
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
        //下单记录
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        return Result.ok(orderId);
    }


}
