package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 19:17
 */
@Controller
public class PaymentController {


    @Autowired
    OrderFeignClient orderFeignClient ;

    @GetMapping("pay.html")
    public String pay(String orderId, Model model){

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay" ;
    }

}
