package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author abt
 * @date 2020/8/18 - 15:35
 */

@RestController
@RequestMapping("api/product")
@CrossOrigin
public class ProductApiController{

    @Autowired
    private SkuService skuService;

    @Autowired
    private SpuService spuService;

    @Autowired
    private BaseCategoryViewService baseCategoryViewService;

    @Autowired
    TrackMarkService trackMarkService ;

    @Autowired
    BaseAttrInfoService baseAttrInfoService;

    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuService.getSkuInfo(skuId);
        return skuInfo;
    }


    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){
        BaseCategoryView baseCategoryView = baseCategoryViewService.getCategoryView(category3Id);
        return baseCategoryView;
    }

    /**
     * 获取sku最新价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable(value = "skuId") Long skuId){

        BigDecimal price = skuService.getSkuPrice(skuId);
        return price;
    }


    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) throws Exception {
        List<SpuSaleAttr>  spuSaleAttrs = spuService.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrs;
    }

    /**
     * 根据spuId 查询数据
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSkuSaleAttrValueListBySpu/{skuId}/{spuId}")
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId){
        List<SkuSaleAttrValue> skuSaleAttrValues = skuService.getSkuSaleAttrValueListBySpu(skuId,spuId);
        return skuSaleAttrValues;
    }

    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){

        Map<String,String> skuValueIdsMap  = skuService.getSkuValueIdsMap(spuId);
        return skuValueIdsMap;
    }

    @GetMapping("getBaseCategoryList")
    Result getBaseCategoryList(){

        List<JSONObject>  baseCategorys = baseCategoryViewService.getBaseCategoryList();
        return Result.ok(baseCategorys);
    }



    @GetMapping("inner/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable("tmId")Long tmId){
        return trackMarkService.getTrademark(tmId);
    }

    /**
     * 通过skuId 集合来查询数据
     * @param skuId
     * @return
     */
    @GetMapping("inner/getAttrList/{skuId}")
    List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId){
        return baseAttrInfoService.getAttrList(skuId);
    }


    @GetMapping("auth/getAttrList/{skuId1}")
    List<BaseAttrInfo> getAttrList1(@PathVariable("skuId1") Long skuId){
        return baseAttrInfoService.getAttrList(skuId);
    }



}
