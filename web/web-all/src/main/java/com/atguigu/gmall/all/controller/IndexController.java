package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.product.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/8/28 - 13:56
 */
@Controller
@RequestMapping("")
public class IndexController {

    @Autowired
    ProductFeignClient productFeignClient ;

    @Autowired
    SpringTemplateEngine templateEngine ;

    @Autowired
    ListFeignClient listFeignClient ;

//    @GetMapping({"/","index.html"})
//    public String index(Model model){
//        Result baseCategoryList = productFeignClient.getBaseCategoryList();
//        model.addAttribute("list", baseCategoryList.getData());
//        return "index/index" ;
//    }

    @GetMapping({"/","index.html"})
    public String index(){
        return "index" ;
    }

    @ResponseBody
    @GetMapping("/createHtml")
    public Result createHtml() throws IOException {

        Result baseCategoryList = productFeignClient.getBaseCategoryList();

        Context context = new Context();

        context.setVariable("list", baseCategoryList.getData());

        String classPath = this.getClass().getClassLoader().getResource("templates").getPath();

        System.out.println("classPath = " + classPath);
        FileWriter write = new FileWriter(classPath+"\\index1.html");
        // 按照index/index.html 模板添加静态页面
        templateEngine.process("index/index.html", context, write);

        return Result.ok();

    }






}

