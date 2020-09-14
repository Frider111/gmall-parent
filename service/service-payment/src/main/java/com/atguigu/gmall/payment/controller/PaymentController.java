package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePageMergePayModel;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.service.impl.PaymentServiceImpl;
import com.atguigu.gmall.payment.util.AliPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/8 - 18:45
 */
@RestController
@RequestMapping("api/payment")
public class PaymentController {

    @Autowired
    PaymentServiceImpl paymentService ;

    @GetMapping("alipay/submit/{orderId}")
    public String aliPaySubmit(@PathVariable String orderId) throws AlipayApiException {

        String body = paymentService.aliPaySubmit(orderId) ;
        return body;
    }

//    http://api.gmall.com/api/payment/alipay/callback/return

    @GetMapping("alipay/callback/return")
    public ModelAndView callbackReturn(@RequestParam Map<String, String> paramMap) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:"+ AliPayConfig.return_order_url);
        return modelAndView;
    }

    @PostMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> paramMap) {

        System.out.println("=======================================");
        
        String trade_no = paramMap.get("trade_no") ;
        String out_trade_no = paramMap.get("out_trade_no") ;
        String trade_status = paramMap.get("trade_status") ;
        String total_amount = paramMap.get("total_amount") ;

        if ("TRADE_SUCCESS".equals(trade_status))
        {
            PaymentInfo paymentInfo  = paymentService.getPayment(out_trade_no) ;

            paymentInfo.setTradeNo(trade_no);
            paymentInfo.setTotalAmount(new BigDecimal(total_amount));
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
            paymentInfo.setCallbackTime(new Date());

            boolean isSuccess = paymentService.updatePayment(paymentInfo);

            if (isSuccess)
            {
                return "success" ;
            }
            return "fail" ;
        }

        return "fail" ;
    }

}
