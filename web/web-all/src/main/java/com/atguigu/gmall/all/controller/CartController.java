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

        // 设置容器
        Context context = new Context();
        // 获取 skuinfo 数据 并放在 context 容器
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 设置 skuinfo 属性值
        context.setVariable("skuInfo", skuInfo);
        // 定位到 static 路径
        String path = this.getClass().getClassLoader().getResource("static").getPath();
        // 封装文件名称
        String cardName = "addCart"+skuId+".html";
        // 设置存放的文件夹
        File cartFile = new File(path+"/cart") ;
        // 不存在文件夹 就创建一个
        if (!cartFile.exists()){
            cartFile.mkdir() ;
        }
        // 字符输出流
        FileWriter fileWriter = new FileWriter(new File(cartFile,cardName));
        // 远程调用，添加数据到购物车
        cartFeignClient.addToCart(skuId,skuNum);
        // 按照 cart/addCart.html 模板 生成数据 填充数据
        springTemplateEngine.process("cart/addCart.html", context,fileWriter);
        // 返回到添加的静态页面
        return "redirect:http://cart.gmall.com/cart/"+cardName;
    }

    @GetMapping("cart.html")
    public String cart() {
        // 去购物车页面
        return "cart/index" ;
    }

}
