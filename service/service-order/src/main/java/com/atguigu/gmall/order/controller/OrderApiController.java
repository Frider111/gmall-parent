package com.atguigu.gmall.order.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
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

        if (orderId.indexOf("库存不足了哦")!=-1)
        {
            return Result.fail().message(orderId) ;
        }

        return Result.ok(orderId);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId){

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return orderInfo ;
    }

    @PutMapping("inner/updateOrderInfo")
    public boolean updateOrderInfo(@RequestBody OrderInfo orderInfo){

        boolean isSuccess = orderService.updateOrderInfo(orderInfo.getId());
        return isSuccess ;
    }



    @GetMapping("auth/{page}/{limit}")
    public Result getOrderPageList(@PathVariable("page") Long page,
                                   @PathVariable("limit") Long limit){

        Page<OrderInfo> orderInfoPage = new Page(page,limit) ;

        orderService.getOrderPageList(orderInfoPage);

        return Result.ok(orderInfoPage) ;
    }

//    http://localhost:8204/api/order/orderSplit?orderId=xxx&wareSkuMap=xxx

    @PostMapping("orderSplit")
    public String orderSplit(String orderId,String wareSkuMap)
    {
        System.out.println("===========================");
//        System.out.println(orderId);
        System.out.println(wareSkuMap);
        List<Map> wareOrderTaskList = JSONObject.parseArray(wareSkuMap,Map.class);
        return orderService.getOrderSplit(orderId,wareOrderTaskList);

//        return null ;
    }

    /**
     * 秒杀提交订单，秒杀订单不需要做前置判断，直接下单
     * @param orderInfo
     * @return
     */
    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo) {
        Long orderId = Long.valueOf(orderService.seckillSubmitOrder(orderInfo));
        return orderId;
    }

}
