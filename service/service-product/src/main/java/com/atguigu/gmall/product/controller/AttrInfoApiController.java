package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 9:31
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class AttrInfoApiController {


    @Autowired
    private BaseAttrInfoService attrService;


    /**
     * 请求地址 ：http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
     * 根据 category3Id 跟等级 获取 attrInfoList 数据，并且统一结果集返回数据
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable("category1Id") Long category1Id,
                               @PathVariable("category2Id") Long category2Id,
                               @PathVariable("category3Id") Long category3Id){

        List<BaseAttrInfo> baseAttrInfos = attrService.attrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfos);
    }

    /**
     * 请求地址 ：http://api.gmall.com/admin/product/saveAttrInfo
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        boolean isSuccess = attrService.insert(baseAttrInfo);
        if(isSuccess)
        {
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 请求地址 ：http://api.gmall.com/admin/product/getAttrValueList/{attrId}
     * @param attrId 通过 分类值，获取属性值
     * @return
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId){

        List<BaseAttrValue> baseAttrValues = attrService.getAttrValueList(attrId);
        return Result.ok(baseAttrValues);
    }


}
