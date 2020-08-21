package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/21 - 16:01
 */
@Service
public class SkuServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuService {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SkuImageService skuImageService;

    /**
     * 根据 skuInfo 添加数据
     * 需要添加的表有
     * sku_info 第一个添加
     * sku_attr_value 需要设置skuid值 =》来源于 sku_info
     * sku_sale_attr_value 需要设置skuid 跟 spuid 值 =》来源于 sku_info
     * sku_image 需要设置 skuid值 =》来源于 sku_info
     * @param skuInfo
     * @return
     */
    @Override
    public boolean saveSkuInfo(SkuInfo skuInfo) {

        boolean isSkuinfo = this.save(skuInfo);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();

        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
        }

        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
        }
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
        }
        boolean isSkuAttrValue = skuAttrValueService.saveBatch(skuAttrValueList);
        boolean isSkuSaleAttrValue = skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        boolean isSkuImage = skuImageService.saveBatch(skuImageList);
        return isSkuinfo && isSkuAttrValue && isSkuSaleAttrValue && isSkuImage;
    }

    @Override
    public void listSku(Page<SkuInfo> page1) {

        this.page(page1);

    }

    @Override
    public boolean onSale(Long skuId) {

        int i = this.baseMapper.onSale(skuId);
        if (i > 0 ){
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelSale(Long skuId) {
        int i = this.baseMapper.cancelSale(skuId);
        if (i > 0 ){
            return true;
        }
        return false;
    }


}
