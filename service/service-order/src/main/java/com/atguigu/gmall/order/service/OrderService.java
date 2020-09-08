package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:38
 */
public interface OrderService {

    Map<String, Object> trade(String userId) ;

    boolean isSubmit(String tradeNo, String userId);

    String submitOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfo(Long orderId);
}
