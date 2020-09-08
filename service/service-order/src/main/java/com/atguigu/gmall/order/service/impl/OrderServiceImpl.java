package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:40
 */
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    UserFeignClient userFeignClient ;

    @Autowired
    CartFeignClient cartFeignClient ;

    @Autowired
    ProductFeignClient productFeignClient ;

    @Autowired
    RedisTemplate redisTemplate ;

    @Autowired
    OrderInfoMapper orderInfoMapper ;

    @Autowired
    OrderDetailService orderDetailService ;

    /**
     * 跳转 trade 页面
     * 需要传递参数 1
     * userAddressList 地址的集合 =》 需要去 userApi 通过 feign 调用 获取数据
     * detailArrayList 订单明细信息的集合，需要通过 feign 调用订单微服务的信息
     * totalNum 总数量 可以通过 detailArrayList 进行计算出来
     * totalAmount 总金额 可以通过 detailArrayList 进行计算出来
     * @return
     */

    @Override
    public Map<String, Object> trade(String userId) {

        Map<String, Object> orderMap = new HashMap<>() ;

        orderMap.put("totalAmount",new BigDecimal(0));

        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(userId);

        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);

        List<OrderDetail> orderDetails = cartCheckedList.stream().map((cartInfo -> {

            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);

            BigDecimal price = productFeignClient.getSkuPrice(cartInfo.getSkuId());

            orderDetail.setOrderPrice(price);

            BigDecimal totalAmoun1t = (BigDecimal)orderMap.get("totalAmount") ;
            orderMap.put("totalAmount",totalAmoun1t.add(price.multiply(new BigDecimal(cartInfo.getSkuNum()))));

            return orderDetail;

        })).collect(Collectors.toList());

        String tradeNo = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("user:"+ userId + ":tradeCode", tradeNo);

        orderMap.put("userAddressList",userAddressListByUserId);
        orderMap.put("detailArrayList",orderDetails);
        orderMap.put("totalNum",orderDetails.size());
        orderMap.put("tradeNo",tradeNo);

        // 把购物车数据添加到 reids 提交订单的时候  通过 redis 获取到数据
        redisTemplate.opsForValue().set("user:"+ userId + ":cartredis", orderDetails);

        return orderMap;
    }

    @Override
    public boolean isSubmit(String tradeNo, String userId) {

        Object redisTradeNo = redisTemplate.opsForValue().get("user:" + userId + ":tradeCode");

       if (redisTradeNo!=null)
       {
//           if (tradeNo.equals(redisTradeNo))
//           {
               redisTemplate.delete("user:" + userId + ":tradeCode");
               return true ;
//           }
       }
       return false;
//        return true ; 测试 redis 方法的时候用到
    }

    @Transactional
    @Override
    public String submitOrder(OrderInfo orderInfo) {

        // 记录总的订单金额
        BigDecimal totalAmount = new BigDecimal(0);

//        List<OrderDetail> orderDetails = orderDetailService.list();

//        List<OrderDetail> orderDetails = orderInfo.getOrderDetailList();
        // 从 redis 获取最新数据
        List<OrderDetail> orderDetails = (List<OrderDetail>) redisTemplate.opsForValue().get("user:"+ orderInfo.getUserId() + ":cartredis");

        String outTradeNo = "BLUE_GRASS"+ System.currentTimeMillis() + ""+ new Random().nextInt(1000);

        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.toString());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.toString());

        for (OrderDetail orderDetail : orderDetails) {

            BigDecimal price = productFeignClient.getSkuPrice(orderDetail.getSkuId());

            if (orderDetail.getOrderPrice().compareTo(price)!=0){
                return "价格发生变化，请刷新页面" ;
            }

            totalAmount = totalAmount.add(price.multiply(new BigDecimal(orderDetail.getSkuNum())));

        }
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setCreateTime(new Date());
        // 设置时长为一天
        orderInfo.setExpireTime(new Date(System.currentTimeMillis()+60*60*24*1000));

        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());

        orderInfoMapper.insert(orderInfo) ;

        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrderId(orderInfo.getId());
        }

        orderDetailService.saveBatch(orderDetails) ;

        redisTemplate.delete("user:"+ orderInfo.getUserId() + ":cartredis");

        return orderInfo.getId()+"";
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return orderInfoMapper.selectById(orderId);
    }
}
