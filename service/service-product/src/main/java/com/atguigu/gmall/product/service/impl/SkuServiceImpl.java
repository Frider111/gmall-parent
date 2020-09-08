package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.aspect.GmallParam;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    ListFeignClient listFeignClient;

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

    /**
     * 分页获取sku集合数据
     * @param page1
     */
    @Override
    public void listSku(Page<SkuInfo> page1) {

        this.page(page1);
//        this.baseMapper.selectPage(page1,null);

    }

    /*
    根据 skuId 进行上架处理
     */
    @Override
    public boolean onSale(Long skuId) {

        int i = this.baseMapper.onSale(skuId);
        if (i > 0 ){
            listFeignClient.onSale(skuId);
            return true;
        }
        return false;
    }

    /**
     * 根据 skuId 进行下架处理
     * @param skuId
     * @return
     */
    @Override
    public boolean cancelSale(Long skuId) {
        int i = this.baseMapper.cancelSale(skuId);
        if (i > 0 ){
            listFeignClient.cancelSale(skuId);
            return true;
        }
        return false;
    }

    /**
     * 获取skuInfo 数据
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX,suffix = RedisConst.SKUKEY_SUFFIX)
    public SkuInfo getSkuInfo(@GmallParam Long skuId) {

        SkuInfo skuinfo = this.getById(skuId);
        // 获取 SkuAttrValue 集合数据
        QueryWrapper<SkuAttrValue> skuAttrValueQueryWrapper = new QueryWrapper<>();
        skuAttrValueQueryWrapper.eq("sku_id", skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueService.list(skuAttrValueQueryWrapper);
        skuinfo.setSkuAttrValueList(skuAttrValues);
        // 获取 SkuSaleAttrValue 集合数据
        QueryWrapper<SkuSaleAttrValue> skuSaleAttrValueQueryWrapper = new QueryWrapper<>();
        skuSaleAttrValueQueryWrapper.eq("sku_id", skuId);
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueService.list(skuSaleAttrValueQueryWrapper);
        skuinfo.setSkuSaleAttrValueList(skuSaleAttrValues);
        // 获取 skuImage 集合数据
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageService.list(skuImageQueryWrapper);
        skuinfo.setSkuImageList(skuImages);
        // 返回数据
        return skuinfo;
    }

    /**
     * 获取价格信息
     * @param skuId 获取skuPrice价格信息
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {

        BigDecimal skuPrice = this.baseMapper.getSkuPrice(skuId);
        if(skuPrice==null){
            return new BigDecimal(0);
        }
        return skuPrice;
    }
    @GmallCache(prefix = "product:",suffix = RedisConst.SKUKEY_SUFFIX)
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(@GmallParam Long skuId,@GmallParam Long spuId) {

        QueryWrapper<SkuSaleAttrValue> skuAttrValuequeryWrapper =  new QueryWrapper<>();
        skuAttrValuequeryWrapper.eq("spu_Id", spuId);
        skuAttrValuequeryWrapper.eq("sKu_Id", skuId);

        List<SkuSaleAttrValue> skuAttrValues = skuSaleAttrValueService.list(skuAttrValuequeryWrapper);
        return skuAttrValues;
    }

    /**
     * 根据spuI的集合 做出以 sku 对应的所有属性集合拼接成key ，通过 key 寻找到 skuId
     * 选择属性的时候，可以通过属性值，同时更新 skuId 在属性页面
     * @param spuId
     * @return
     */
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX,suffix = "valuemap")
    @Override
    public Map<String, String> getSkuValueIdsMap(Long spuId) {
        // 封装的 map 集合 key value 在方法上面有解释
        Map<String,String> skuValueIdMap = new HashMap<>();
        // sql 返回的 List map 数据 ，取出 map 数据 进行 返回值 map 封装
        List<Map<String, Object>> skuValueIdsMaps = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);

        for (Map<String, Object> skuValueMap : skuValueIdsMaps) {
            skuValueIdMap.put(skuValueMap.get("skuValue")+"",skuValueMap.get("sku_id")+"");
        }
        
        return skuValueIdMap;
    }
}
