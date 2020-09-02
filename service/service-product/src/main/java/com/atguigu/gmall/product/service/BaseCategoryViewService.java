package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 9:25
 */
public interface BaseCategoryViewService   {
    BaseCategoryView getCategoryView(Long category3Id);

    List<JSONObject> getBaseCategoryList();


}
