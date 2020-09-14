package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:03
 */
@RequestMapping("")
@Controller
public class OrderController {


    @Autowired
    OrderFeignClient orderFeignClient ;

    @GetMapping("trade.html")
    public String trade(Model model){

        Result<Map<String, Object>> result = orderFeignClient.trade();

        model.addAllAttributes(result.getData());

        return "order/trade" ;
    }

    @GetMapping("myOrder.html")
    public String myOrder()
    {
        
        return "order/myOrder" ;
    }

}
