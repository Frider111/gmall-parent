package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 9:36
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper,BaseAttrInfo> implements BaseAttrInfoService {


    @Autowired
    private BaseAttrValueService baseAttrValueService;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;


    /**
     * 1，	平台属性可以挂在一级分类、二级分类和三级分类；
     * 2，	查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性；
     * 3，	查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     * 取出对应一级分类下面的平台属性与二级分类对应的平台属性；
     * 4，	查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；取出对应一级分类、二级分类与三级分类对应的平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        // 根据category_id，category_level 跟查询BaseAttrInfos 集合

        QueryWrapper<BaseAttrInfo> infoQueryWrapper = new QueryWrapper<>();
        // 如果一级分类都是等于 0 说明还没选择，返回null
        if (category1Id==0){
            return null ;
        }
        // 如果二级分类等于 0 说明只有一级分类
        else if(category2Id == 0 ){

            infoQueryWrapper.eq("category_id", category1Id);
            infoQueryWrapper.eq("category_level", 1);

        }else if(category3Id == 0){
        // 如果三级分类等于 0 说明现在查询的是二级分类
            infoQueryWrapper.eq("category_id", category2Id);
            infoQueryWrapper.eq("category_level", 2);

        }else {
        // 其他情况，就是3个都选择了，就是查询3级分类
            infoQueryWrapper.eq("category_id", category3Id);
            infoQueryWrapper.eq("category_level", 3);

        }
        // 获取到list集合
        List<BaseAttrInfo> baseAttrInfos = this.list(infoQueryWrapper);

        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
            // 根据attr_id查询attr value 数据集合额
            QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("attr_id", baseAttrInfo.getId());
            List<BaseAttrValue> baseAttrValues = baseAttrValueService.list(valueQueryWrapper);
            // 将查询到的list集合存入到baseAttrInfos中的属性setAttrValueList
            baseAttrInfo.setAttrValueList(baseAttrValues);

        }
        // 返回list集合
        return baseAttrInfos;
    }

    /**
     * 前台传递 baseAttrInfo
     * 往 base_attr_info 添加数据，返回添加数据的id
     * 然后根据 base_attr_info 添加进去的 id
     * @param baseAttrInfo
     * @return
     */
    @Transactional
    @Override
    public boolean insert(BaseAttrInfo baseAttrInfo) {

        if(baseAttrInfo.getId()!=null){
            //先删除属性值数据
            QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueService.remove(valueQueryWrapper);
        }
        boolean infoSuccess = this.saveOrUpdate(baseAttrInfo);
        // 设置value数据的attr属性
        for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
        }
        // 重新添加数据
        boolean valueSuccess = baseAttrValueService.saveBatch(baseAttrInfo.getAttrValueList());

        return infoSuccess && valueSuccess;
    }

    /**
     * 根据 attrId 查询数据
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {

        QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
        valueQueryWrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueService.list(valueQueryWrapper);
        return baseAttrValues;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.getAttrList(skuId);
    }
}
