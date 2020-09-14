package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:38
 */
public interface OrderService {

    Map<String, Object> trade(String userId) ;

    boolean isSubmit(String tradeNo, String userId);

    String submitOrder(OrderInfo orderInfo);

    Long seckillSubmitOrder(OrderInfo orderInfo);

    OrderInfo getOrderInfo(Long orderId);

    boolean updateOrderInfo(Long orderId);

    void getOrderPageList(Page<OrderInfo> orderInfoPage);

    /**
     * 处理过期订单
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    /**
     * 根据订单Id 修改订单的状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);


    void sendOrderStatus(String orderId);

    String getOrderSplit(String orderId, List<Map> wareListMap);
}
