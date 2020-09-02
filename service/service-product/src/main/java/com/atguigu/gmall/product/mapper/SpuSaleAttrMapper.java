package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 15:31
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {


    List<SpuSaleAttrValue> getSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId") Long spuId);


    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku1(@Param("skuId") Long skuId, @Param("spuId") Long spuId);


}
