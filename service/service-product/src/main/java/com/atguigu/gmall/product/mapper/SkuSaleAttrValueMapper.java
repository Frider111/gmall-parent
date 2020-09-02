package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author abt
 * @date 2020/8/21 - 16:13
 */
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {


    List<Map<String, Object>> getSkuValueIdsMap(@Param("spuId") Long spuId);
}
