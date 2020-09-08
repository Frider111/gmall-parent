package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.service.impl.CartInfoServiceImpl;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 20:57
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartInfoServiceImpl cartInfoService ;

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public void addToCart(@PathVariable("skuId") Long skuId,
                          @PathVariable("skuNum") Integer skuNum,
                          HttpServletRequest request){

        String userId = request.getHeader("userId");

        if (StringUtils.isBlank(userId))
        {
            userId = request.getHeader("userTempId");
        }

        cartInfoService.addToCart(skuId,skuNum,userId) ;
    }

    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request){

        String userId = request.getHeader("userId");


        String userTempId = request.getHeader("userTempId");

        List<CartInfo> cartList = cartInfoService.cartList(userId,userTempId);

        return Result.ok(cartList) ;
    }

    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId
            ,@PathVariable("isChecked") Integer isChecked
            ,HttpServletRequest request){

        String userId = request.getHeader("userId");

        if (StringUtils.isBlank(userId))
        {
            userId = request.getHeader("userTempId");
        }
        cartInfoService.checkCart(skuId,isChecked,userId);

        return Result.ok() ;
    }

    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId
            ,HttpServletRequest request){

        String userId = request.getHeader("userId");


        if (StringUtils.isBlank(userId))
        {
            userId = request.getHeader("userTempId");
        }
        cartInfoService.deleteCart(skuId,userId);

        return Result.ok() ;
    }

    @GetMapping("getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId){


        List<CartInfo> cartInfos = cartInfoService.getCartCheckedList(userId) ;


        return cartInfos;
    }

}
