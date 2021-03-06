package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:18
 */
@FeignClient("SERVICE-ORDER")
public interface OrderFeignClient {


    @GetMapping("api/order/auth/trade")
    Result<Map<String, Object>> trade() ;

    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId);


    @PutMapping("api/order/inner/updateOrderInfo")
    boolean updateOrderInfo(@RequestBody OrderInfo orderInfo) ;

    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);


}
