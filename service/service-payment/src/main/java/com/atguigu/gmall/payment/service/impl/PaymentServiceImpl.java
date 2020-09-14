package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.util.AliPayConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/8 - 18:50
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    AlipayClient alipayClient1;

    @Autowired
    PaymentMapper paymentMapper;

    @Autowired
    RabbitService rabbitService;

    @Override
    public String aliPaySubmit(String orderId) throws AlipayApiException {


        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();

        Map<String, Object> paramMap = new HashMap<>();

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.valueOf(orderId));

        StringBuilder subject = new StringBuilder("");

        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            subject.append(orderDetail.getSkuName());
        }

        boolean isSuccess = savePayment(orderInfo, subject.toString());

        if (isSuccess == false) {
            return null;
        }

        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        paramMap.put("total_amount", 0.01);
        paramMap.put("subject", subject);

        alipayTradePagePayRequest.setBizContent(JSONObject.toJSONString(paramMap));

        // 设置同步地址
        alipayTradePagePayRequest.setReturnUrl(AliPayConfig.return_payment_url);
        // 设置异步地址
        alipayTradePagePayRequest.setNotifyUrl(AliPayConfig.notify_payment_url);

        AlipayTradePagePayResponse alipayTradePagePayResponse = alipayClient1.pageExecute(alipayTradePagePayRequest);

        return alipayTradePagePayResponse.getBody();

    }

    @Override
    public PaymentInfo getPayment(String out_trade_no) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("out_trade_no", out_trade_no);

        PaymentInfo paymentInfo = paymentMapper.selectOne(queryWrapper);

        return paymentInfo;
    }

    /**
     * 修改订单状态
     * @param paymentInfo
     * @return
     */
    @Transactional
    @Override
    public boolean updatePayment(PaymentInfo paymentInfo) {

        int changeNum = paymentMapper.updateById(paymentInfo);

        if (changeNum > 0) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
            return true;
        }
        return false;
    }

    /**
     * 查询接口
     * @param tradeNo
     * @return
     */
    @Override
    public boolean tradeNoQuery(String tradeNo) throws Exception {

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String,Object> params = new HashMap<>() ;

        params.put("trade_no",tradeNo) ;

        request.setBizContent(JSONObject.toJSONString(params));

        AlipayTradeQueryResponse response = alipayClient1.execute(request);

        String tradeStatus = response.getTradeStatus();

        if ("TRADE_SUCCESS".equals(tradeStatus))
        {
            QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper() ;

            queryWrapper.eq("", tradeNo) ;

            PaymentInfo paymentInfo = paymentMapper.selectOne(queryWrapper);

            paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());

            updatePayment(paymentInfo);
        }

        return false;
    }

    /**
     *
     *
     * @param orderInfo
     * @param subject
     * @return
     */
    public boolean savePayment(OrderInfo orderInfo, String subject) {

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(PaymentType.ALIPAY.toString());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(subject);
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setCreateTime(new Date());

        int num = paymentMapper.insert(paymentInfo);

        if (num > 0) {
            return true;
        }

        return false;
    }

}
