package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:14
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {


    @Autowired
    OrderService orderService ;

    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request){


        String userId = request.getHeader("userId");

        Map<String, Object>  orderMap = orderService.trade(userId);

        return Result.ok(orderMap) ;

    }

//    POST http://api.gmall.com/api/order/auth/submitOrder?tradeNo=70453c77-2bff-487e-a8f1-d695b0b852d9

    @PostMapping("auth/submitOrder")
    public Result submitOrder(String tradeNo, @RequestBody OrderInfo orderInfo,HttpServletRequest request){

        String userId = request.getHeader("userId");

        boolean isSubmit = orderService.isSubmit(tradeNo,userId);

        if (isSubmit==false){
            return Result.fail().message("订单生成失败。。。啦啦啦啦啦") ;
        }

        orderInfo.setUserId(Long.valueOf(userId));
        String orderId = orderService.submitOrder(orderInfo) ;

        if ("价格发生变化，请刷新页面".equals(orderId)){
            return Result.fail().message(orderId) ;
        }

        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId){

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return orderInfo ;
    }

}
