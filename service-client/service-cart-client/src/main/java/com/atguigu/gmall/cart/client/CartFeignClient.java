package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 21:01
 */
@FeignClient("SERVICE-CART")
public interface CartFeignClient {

    @PostMapping("api/cart/addToCart/{skuId}/{skuNum}")
    void addToCart(@PathVariable("skuId") Long skuId,@PathVariable("skuNum") Integer skuNum) ;

    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId);

}
