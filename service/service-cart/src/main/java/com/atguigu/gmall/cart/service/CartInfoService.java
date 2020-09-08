package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/5 - 19:16
 */
public interface CartInfoService extends IService <CartInfo> {

    void addToCart(Long skuId,Integer skuNum,String userId);

    List<CartInfo> cartList(String userId,String userTempId);

    void checkCart(Long skuId, Integer isChecked, String userId);

    void deleteCart(Long skuId, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
