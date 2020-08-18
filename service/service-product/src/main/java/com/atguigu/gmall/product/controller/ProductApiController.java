package com.atguigu.gmall.product.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author abt
 * @date 2020/8/18 - 15:35
 */
@RestController
@RequestMapping("admin/product")
public class ProductApiController {

    @RequestMapping("proTest")
    public String proTest(){
        return "测试123";
    }

}
