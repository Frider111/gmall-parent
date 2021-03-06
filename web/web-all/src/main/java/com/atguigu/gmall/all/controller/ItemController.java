package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.execption.GmallExecption;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/8/22 - 22:15
 */
@Controller
@RequestMapping
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;


    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) throws Exception{

        Result<Map> result = itemFeignClient.getItem(skuId);
        if (result !=null){

            model.addAllAttributes(result.getData());
        }
        else {
            throw new GmallExecption("这是一个超时异常，请重新尝试一波");
        }
        return "item/index";
    }

}


