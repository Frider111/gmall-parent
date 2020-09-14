package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.model.payment.PaymentInfo;

/**
 * @author Blue Grass
 * @date 2020/9/8 - 18:49
 */
public interface PaymentService {

    String aliPaySubmit(String orderId) throws AlipayApiException;

    PaymentInfo getPayment(String out_trade_no);

    boolean updatePayment(PaymentInfo paymentInfo);

    boolean tradeNoQuery(String tradeNo) throws Exception;

}
