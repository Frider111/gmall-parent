package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.BaseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 9:30
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class CategoryApiController {


    @Autowired
    private BaseCategoryService baseCategoryService;


    /**
     * 前端调用地址：http://api.gmall.com/admin/product/getCategory1
     * 获取以及分类的list集合
     * @return
     */
    @GetMapping("getCategory1")
    public Result getCategory1(){

        List<BaseCategory1> baseCategory1 = baseCategoryService.getCategory1();
        Result result = Result.ok(baseCategory1);
        return result;
    }


    /**
     * 前端调用地址：http://api.gmall.com/admin/product/getCategory2/{category1Id}
     * 根据category1Id 获取二级分类的list集合
     * @param category1Id
     * @return
     */
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){

        List<BaseCategory2> baseCategory2 = baseCategoryService.getCategory2(category1Id);
        Result result = Result.ok(baseCategory2);
        return result;
    }


    /**
     * 前端调用地址 ： http://api.gmall.com/admin/product/getCategory3/{category2Id}
     * category2Id 获取二级分类的list集合
     * @param category2Id
     * @return
     */
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){

        List<BaseCategory3> baseCategory2 = baseCategoryService.getCategory3(category2Id);
        Result result = Result.ok(baseCategory2);
        return result;
    }


}
