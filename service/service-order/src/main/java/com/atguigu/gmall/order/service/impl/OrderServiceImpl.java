package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentType;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.aspectj.weaver.ast.Var;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    UserFeignClient userFeignClient;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailService orderDetailService;

    @Autowired
    RabbitService rabbitService;

    /**
     * 跳转 trade 页面
     * 需要传递参数 1
     * userAddressList 地址的集合 =》 需要去 userApi 通过 feign 调用 获取数据
     * detailArrayList 订单明细信息的集合，需要通过 feign 调用订单微服务的信息
     * totalNum 总数量 可以通过 detailArrayList 进行计算出来
     * totalAmount 总金额 可以通过 detailArrayList 进行计算出来
     *
     * @return
     */

    @Override
    public Map<String, Object> trade(String userId) {

        Map<String, Object> orderMap = new HashMap<>();

        orderMap.put("totalAmount", new BigDecimal(0));

        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(userId);

        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);

        List<OrderDetail> orderDetails = cartCheckedList.stream().map((cartInfo -> {

            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);

            BigDecimal price = productFeignClient.getSkuPrice(cartInfo.getSkuId());

            orderDetail.setOrderPrice(price);

            BigDecimal totalAmoun1t = (BigDecimal) orderMap.get("totalAmount");
            orderMap.put("totalAmount", totalAmoun1t.add(price.multiply(new BigDecimal(cartInfo.getSkuNum()))));

            return orderDetail;

        })).collect(Collectors.toList());

        String tradeNo = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("user:" + userId + ":tradeCode", tradeNo);

        orderMap.put("userAddressList", userAddressListByUserId);
        orderMap.put("detailArrayList", orderDetails);
        orderMap.put("totalNum", orderDetails.size());
        orderMap.put("tradeNo", tradeNo);

        // 把购物车数据添加到 reids 提交订单的时候  通过 redis 获取到数据
        redisTemplate.opsForValue().set("user:" + userId + ":cartredis", orderDetails);

        return orderMap;
    }

    @Override
    public boolean isSubmit(String tradeNo, String userId) {

        Object redisTradeNo = redisTemplate.opsForValue().get("user:" + userId + ":tradeCode");

        if (redisTradeNo != null) {
//           if (tradeNo.equals(redisTradeNo))
//           {
            redisTemplate.delete("user:" + userId + ":tradeCode");
            return true;
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
        List<OrderDetail> orderDetails = (List<OrderDetail>) redisTemplate.opsForValue().get("user:" + orderInfo.getUserId() + ":cartredis");

        orderDetails = orderInfo.getOrderDetailList() ;

        String outTradeNo = "BLUE_GRASS" + System.currentTimeMillis() + "" + new Random().nextInt(1000);

        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.toString());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.toString());

        for (OrderDetail orderDetail : orderDetails) {

            BigDecimal price = productFeignClient.getSkuPrice(orderDetail.getSkuId());

            if (orderDetail.getOrderPrice().compareTo(price) != 0) {
                return "价格发生变化，请刷新页面";
            }

            String isK = HttpClientUtil.doGet("http://localhost:9001/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());

            if ("0".equals(isK)) {
                return "这个" + orderDetail.getSkuName() + "库存不足了哦";
            }

            totalAmount = totalAmount.add(price.multiply(new BigDecimal(orderDetail.getSkuNum())));

        }
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setCreateTime(new Date());
        // 设置时长为一天
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000));

        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());

        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrderId(orderInfo.getId());
        }

        orderDetailService.saveBatch(orderDetails);

        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL, orderInfo.getId());

        redisTemplate.delete("user:" + orderInfo.getUserId() + ":cartredis");

        return orderInfo.getId() + "";
    }

    @Override
    public Long seckillSubmitOrder(OrderInfo orderInfo) {
        // 记录总的订单金额
        BigDecimal totalAmount = new BigDecimal(0);

//        List<OrderDetail> orderDetails = orderDetailService.list();

//        List<OrderDetail> orderDetails = orderInfo.getOrderDetailList();
        // 从 redis 获取最新数据

        List<OrderDetail> orderDetails = orderInfo.getOrderDetailList() ;

        String outTradeNo = "BLUE_GRASS" + System.currentTimeMillis() + "" + new Random().nextInt(1000);

        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.toString());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.toString());
        orderInfo.setTotalAmount(orderInfo.getTotalAmount());
        orderInfo.setCreateTime(new Date());
        // 设置时长为一天
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000));

        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());

        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrderId(orderInfo.getId());
        }

        orderDetailService.saveBatch(orderDetails);

        rabbitService.sendDelayMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL, orderInfo.getId());

        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

        QueryWrapper<OrderDetail> orderWrapper = new QueryWrapper<>();

        orderWrapper.eq("order_id", orderId);

        List<OrderDetail> list = orderDetailService.list(orderWrapper);

        orderInfo.setOrderDetailList(list);

        return orderInfo;
    }

    /**
     * 订单完成后修改信息
     *
     * @param orderId
     * @return
     */
    @Override
    public boolean updateOrderInfo(Long orderId) {

        OrderInfo orderInfo = getOrderInfo(orderId);

        orderInfo.setOrderStatus(OrderStatus.PAID.toString());
        orderInfo.setPaymentWay(PaymentType.ALIPAY.toString());
        orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000 * 9999));
        orderInfo.setProcessStatus(ProcessStatus.PAID.toString());

        int changeNum = orderInfoMapper.updateById(orderInfo);

        if (changeNum > 0) {
            return true;
        }

        return false;
    }

    /**
     * 分页获取订单信息
     *
     * @param orderInfoPage
     */
    @Override
    public void getOrderPageList(Page<OrderInfo> orderInfoPage) {

        QueryWrapper<OrderInfo> queryWrapper1 = new QueryWrapper<>();

        queryWrapper1.orderByDesc("create_time");

        orderInfoMapper.selectPage(orderInfoPage, queryWrapper1);

        List<OrderInfo> records = orderInfoPage.getRecords();

        for (OrderInfo record : records) {

            List<OrderDetail> orderDetails = new ArrayList<>();

            QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();

            queryWrapper.eq("order_id", record.getId());

            List<OrderDetail> orderDetails1 = orderDetailService.list(queryWrapper);

            record.setOrderDetailList(orderDetails1);
        }

    }

    /**
     * 关闭订单
     *
     * @param orderId
     */
    @Override
    public void execExpiredOrder(Long orderId) {
        updateOrderStatus(orderId, ProcessStatus.CLOSED);
    }

    /**
     * 修改订单状态
     *
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfoMapper.updateById(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {

        String wareJson = initWareOrder(orderId);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK, MqConst.ROUTING_WARE_STOCK, wareJson);

    }

    @Override
    public String getOrderSplit(String orderId, List<Map> wareListMap) {

        OrderInfo orderInfo = this.orderInfoMapper.selectById(orderId);

        List<Map<String, Object>> orders = new ArrayList<>();

        for (Map wareSkuMap : wareListMap) {

            HashMap<String, Object> map = new HashMap<>();

            map.put("orderId", orderInfo.getId());
            map.put("consignee", orderInfo.getConsignee());
            map.put("consigneeTel", orderInfo.getConsigneeTel());
            map.put("orderComment", orderInfo.getOrderComment());
            map.put("orderBody", orderInfo.getTradeBody());
            map.put("deliveryAddress", orderInfo.getDeliveryAddress());
            map.put("paymentWay", orderInfo.getPaymentWay());
            map.put("wareId", wareSkuMap.get("wareId"));// 仓库Id ，减库存拆单时需要使用！
            map.put("createTime", orderInfo.getCreateTime());

            JSONArray skuIds = (JSONArray)wareSkuMap.get("skuIds");
            List<Map> maps = new ArrayList<>();
            for (Object skuId : skuIds) {
                QueryWrapper<OrderDetail> orderInfoQueryWrapper = new QueryWrapper<>();

                orderInfoQueryWrapper.eq("order_id", orderId);
                orderInfoQueryWrapper.eq("sku_id", skuId+"");

                List<OrderDetail> orderDetails = orderDetailService.list(orderInfoQueryWrapper);
                for (OrderDetail orderDetail : orderDetails) {

                    HashMap<String, Object> orderDetailMap = new HashMap<>();
                    orderDetailMap.put("skuId", orderDetail.getSkuId());
                    orderDetailMap.put("skuNum", orderDetail.getSkuNum());
                    orderDetailMap.put("skuName", orderDetail.getSkuName());
                    maps.add(orderDetailMap);
                }
            }
            map.put("details", maps);
            orders.add(map);
            }

        return JSONObject.toJSONString(orders);
    }

    /**
     * 给库存的参数
     *
     * @param orderId
     * @return
     */
    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = this.orderInfoMapper.selectById(orderId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", orderInfo.getPaymentWay());
        map.put("wareId", orderInfo.getWareId());// 仓库Id ，减库存拆单时需要使用！
        map.put("createTime", orderInfo.getCreateTime());


        QueryWrapper<OrderDetail> orderInfoQueryWrapper = new QueryWrapper<>();

        orderInfoQueryWrapper.eq("order_id", orderId);

        List<OrderDetail> orderDetails = orderDetailService.list(orderInfoQueryWrapper);

        List<Map> maps = new ArrayList<>();

        for (OrderDetail orderDetail : orderDetails) {

            HashMap<String, Object> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId", orderDetail.getSkuId());
            orderDetailMap.put("skuNum", orderDetail.getSkuNum());
            orderDetailMap.put("skuName", orderDetail.getSkuName());
            maps.add(orderDetailMap);
        }

        map.put("details", maps);

        return JSONObject.toJSONString(map);
    }

}
