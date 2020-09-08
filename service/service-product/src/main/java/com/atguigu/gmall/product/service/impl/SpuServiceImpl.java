package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.aspect.GmallParam;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 14:42
 */
@Service
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuInfo> implements SpuService {

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuSaleAttrService spuSaleAttrService;

    @Autowired
    private SpuSaleAttrValueService spuSaleAttrValueService;

    @Autowired
    private SpuImageService spuImageService;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;


    @Override
    public void listSpu(Page<SpuInfo> page1, Long category3Id) {

        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        // 根据查询对象 查询数据
        wrapper.eq("category3_id", category3Id);
        // 结果集封装
        this.page(page1, wrapper);
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 根据 spuInfo 添加数据
     * 添加4个表
     * 第一 ：spu_info
     * 第二 : spu_image 需要设置 spuid
     * 第三 ：spu_sale_attr 需要设置 spuid
     * 第四 ：spu_sale_attr_value 需要设置spuid 跟 spu_sale_attr名称
     * @param spuInfo
     * @return
     */
    @Transactional
    @Override
    public boolean insert(SpuInfo spuInfo) {

        // 添加SpuInfo数据
        boolean isSpuInfo = this.save(spuInfo);
        // 获取 spuImageList 数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        // 给 spuImageList 设置spuid
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
        }
        // 给spuSaleAttrList设置spuid
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            // 给spuSaleAttrValueList 设置spuid数据 以及设置他的销售属性名称
            List<SpuSaleAttrValue> spuSaleAttrValueList= spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
            }
            spuSaleAttrValueService.saveBatch(spuSaleAttrValueList); // 这里没做返回值，添加失败的话，也没反应
        }
        // 添加数据
        boolean isSpuSaleAttr = spuSaleAttrService.saveBatch(spuSaleAttrList);
        boolean isSpuImage = spuImageService.saveBatch(spuImageList);
        return isSpuInfo && isSpuSaleAttr && isSpuImage;
    }

    /**
     * 根据 spuID 获取spu-image集合信息
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {

        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImages = spuImageService.list(spuImageQueryWrapper);
        return spuImages;
    }

    /**
     * 根据spuId获取平台属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
        spuSaleAttrQueryWrapper.eq("spu_id",spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrService.list(spuSaleAttrQueryWrapper);
        // 根据属性获取属性值
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {

            QueryWrapper<SpuSaleAttrValue> spuSaleAttrValueQueryWrapper = new QueryWrapper<>();
            spuSaleAttrValueQueryWrapper.eq("spu_id", spuId);
            spuSaleAttrValueQueryWrapper.eq("base_sale_attr_id", spuSaleAttr.getBaseSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueService.list(spuSaleAttrValueQueryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }
        
        return spuSaleAttrs;
    }

    @GmallCache(prefix = "spu:",suffix = ":saleattr")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@GmallParam Long skuId, @GmallParam Long spuId) throws Exception {

        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku1(skuId,spuId);
    }

//    /**
//     * 换成另一个牛逼的了
//     * 根据 skuId 跟 spuId 获取 SpuSaleAttr 销售属性的集合
//     * 如果 spu_attr_value 数据跟 skuSaleAttrValue 中可以关联上 icCheck 赋值等于1 【spu_sale_attr_value.id == sku.sale_attr_value_id】
//     *
//     * @param skuId
//     * @param spuId
//     * @return
//     */
//    @Override
//    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
//
//        // 先查询一波 SpuSaleAttr 集合数据 【根据spuId】
//        QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
//        spuSaleAttrQueryWrapper.eq("spu_id", spuId);
//        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrService.list(spuSaleAttrQueryWrapper);
//
//        // 获取 spuSaleAttrValues 集合数据 ，如果可以关联上 isCheck 赋值等于 1
//        List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
//
//        Map<String,SpuSaleAttr> spuSaleAttrMap = new HashMap<>();
//
//        // 先把 spuSaleAttrs 封装到 map 里面 ，避免双重 for 循环查询 SpuSaleAttrValue 数据 【影响性能】
//        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
//            // 因为需要两个键值 封装数据，所以用中间随机一个字符串连接数据，避免数据冲突
//            spuSaleAttrMap.put(spuSaleAttr.getSpuId()+"concat"+spuSaleAttr.getBaseSaleAttrId(),spuSaleAttr);
//        }
//
//        // 根据 SpuSaleAttrValue 以同样规则设置 key 值，通过 key 获取spuSaleAttr对象，在通过对象进行 SaleAttrValueList 数据添加
//        for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValues) {
//
//            SpuSaleAttr spuSaleAttr = spuSaleAttrMap.get(spuSaleAttrValue.getSpuId() + "concat" + spuSaleAttrValue.getBaseSaleAttrId());
//            spuSaleAttr.getSpuSaleAttrValueList().add(spuSaleAttrValue);
//        }
//
////        双重循环解决数据封装问题
////        for (int i = 0; i < spuSaleAttrs.size(); i++) {
////            for (int j = 0; j < spuSaleAttrValues.size(); j++) {
////                if(spuSaleAttrs.get(i).getBaseSaleAttrId() == spuSaleAttrValues.get(j).getBaseSaleAttrId()){
////                    spuSaleAttrs.get(i).getSpuSaleAttrValueList().add(spuSaleAttrValues.get(j));
////                }
////            }
////        }
//        return spuSaleAttrs;
//    }

}
