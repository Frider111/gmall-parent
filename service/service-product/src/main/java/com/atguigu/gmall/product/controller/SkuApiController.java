package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author abt
 * @date 2020/8/21 - 15:30
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SkuApiController {


    @Autowired
    private SkuService skuService;

    /**
     * 请求路径 ：http://api.gmall.com/admin/product/saveSkuInfo
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){

        boolean isSuccess = skuService.saveSkuInfo(skuInfo);
        if(isSuccess)
        {
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 前端访问地址 ：http://api.gmall.com/admin/product/list/{page}/{limit}
     * @param page  表示当前页数和
     * @param limit 表示当前页显示条数
     * @return
     */
    @GetMapping("list/{page}/{limit}")
    public Result listSpu(@PathVariable("page") Long page,
                          @PathVariable("limit") Long limit){
        // 封装一个page对象
        Page<SkuInfo> page1 = new Page<>(page, limit);
        // 对分页对象进行设置结果集
        skuService.listSku(page1);
        return Result.ok(page1);
    }


    /**
     * 前端访问地址 ：http://api.gmall.com/admin/product/onSale/{skuId}
     * @param skuId  根据skuId判断是否上架
     * @return
     */
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){

        boolean isSuccess = skuService.onSale(skuId);
        if(isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 前端访问地址 ：http://api.gmall.com/admin/product/cancelSale/{skuId}
     * @param skuId  根据skuId判断是否上架
     * @return
     */
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){

        boolean isSuccess = skuService.cancelSale(skuId);
        if(isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }


}
