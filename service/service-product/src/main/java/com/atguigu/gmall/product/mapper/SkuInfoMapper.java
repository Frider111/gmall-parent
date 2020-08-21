package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author abt
 * @date 2020/8/21 - 16:02
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    @Update("update sku_info set is_sale = 1 where id =  #{skuId}")
    int onSale(Long skuId);

    @Update("update sku_info set is_sale = 0 where id =  #{skuId}")
    int cancelSale(Long skuId);

}
