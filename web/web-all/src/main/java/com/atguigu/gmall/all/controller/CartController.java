package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 17:47
 */
@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient ;

    @Autowired
    ProductFeignClient productFeignClient ;

    @Autowired
    SpringTemplateEngine springTemplateEngine ;


    @GetMapping("addCart.html")
    public String addCart(Long skuId,Integer skuNum) throws IOException {

        Context context = new Context();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        context.setVariable("skuInfo", skuInfo);

        String path = this.getClass().getClassLoader().getResource("static").getPath();

        String cardName = "addCart"+skuId+".html";

        File cartFile = new File(path+"/cart") ;

        if (!cartFile.exists()){
            cartFile.mkdir() ;
        }

        FileWriter fileWriter = new FileWriter(new File(cartFile,cardName));

        springTemplateEngine.process("cart/addCart.html", context,fileWriter);

        cartFeignClient.addToCart(skuId,skuNum);

        return "redirect:http://cart.gmall.com/cart/"+cardName;
    }

    @GetMapping("cart.html")
    public String cart() {

        return "cart/index" ;
    }

}
