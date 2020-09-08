package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 14:33
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SpuInfoApiController {

    @Autowired
    private SpuService spuService;

    /**
     * 前端访问地址 ：http://api.gmall.com/admin/product/ {page}/{limit}?category3Id=61
     *
     * @param page  表示当前页数和
     * @param limit 表示当前页显示条数
     * @param category3Id 根据category3Id查询数据
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result listSpu(@PathVariable("page") Long page,
                          @PathVariable("limit") Long limit,
                          Long category3Id){
        // 封装一个page对象
        Page<SpuInfo> page1 = new Page<>(page, limit);
        // 对分页对象进行设置结果集
        spuService.listSpu(page1,category3Id);

        return Result.ok(page1);
    }

//    http://api.gmall.com/admin/product/baseSaleAttrList

    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrs = spuService.baseSaleAttrList();
        return  Result.ok(baseSaleAttrs);
    }

    /**
     * 请求地址 ：http://api.gmall.com/admin/product/saveSpuInfo
     * @param spuInfo
     * @return
     */
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        boolean isSuccess = spuService.insert(spuInfo);
        if(isSuccess)
        {
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 请求路径： http://api.gmall.com/admin/product/spuImageList/{spuId}
     * 根据 spuid 获取请求路径 获取 spuImageList 集合
     * @param spuId
     * @return
     */
    @GetMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable Long spuId){

        List<SpuImage> spuImages = spuService.spuImageList(spuId);
        return Result.ok(spuImages);
    }

    /**
     * 请求路径： http://api.gmall.com/admin/product/spuSaleAttrList/{spuId}
     * 根据 spuid 获取请求路径 获取 spuSaleAttrList 集合
     * @param spuId
     * @return
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){

        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }


}
