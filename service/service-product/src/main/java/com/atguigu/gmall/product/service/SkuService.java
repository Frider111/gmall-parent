package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author abt
 * @date 2020/8/21 - 16:00
 */
public interface SkuService extends IService<SkuInfo> {

    boolean saveSkuInfo(SkuInfo skuInfo);

    void listSku(Page<SkuInfo> page1);

    boolean onSale(Long skuId);

    boolean cancelSale(Long skuId);

    SkuInfo getSkuInfo(Long skuId);

    BigDecimal getSkuPrice(Long skuId);


    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(Long skuId, Long spuId);

    Map<String, String> getSkuValueIdsMap(Long spuId);
}
