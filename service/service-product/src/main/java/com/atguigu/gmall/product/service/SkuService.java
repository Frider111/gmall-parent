package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author abt
 * @date 2020/8/21 - 16:00
 */
public interface SkuService extends IService<SkuInfo> {
    boolean saveSkuInfo(SkuInfo skuInfo);

    void listSku(Page<SkuInfo> page1);

    boolean onSale(Long skuId);

    boolean cancelSale(Long skuId);
}
