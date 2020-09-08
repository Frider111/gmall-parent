package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.time.GmallChangeTime;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 9:25
 */
@Service
public class BaseCategoryViewServiceImpl implements BaseCategoryViewService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 根据id获取分类信息
     * @param category3Id
     * @return
     */
    @GmallCache(prefix = "base:" , suffix = ":category")
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {


        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 将BaseCategoryView数据，返回一个 List<JSONObject> 数据
     * @return
     */
    @GmallChangeTime
    @GmallCache(prefix = "baseCategory:",suffix = ":list")
    @Override
    public List<JSONObject> getBaseCategoryList() {

        // 创建一个返回的JSON对象
        List<JSONObject> jsonObjects = new ArrayList();
        // 从数据库放取结果集数据
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        // 从结果集按照 baseCategory1 分组
        Map<Long, List<BaseCategoryView>> baseCategory1 = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        // 遍历 baseCategory1 这个 map 数据
        baseCategory1.forEach((category1Id,baseCategory1s) -> {
            //  创建一个临时使用的 JSONObject 对象，到时候存入到 jsonObjects 集合当中
            JSONObject jsonObject1 = new JSONObject();
            // 添加 categoryId 数据
            jsonObject1.put("categoryId",category1Id);
            // 添加 categoryName 数据
            jsonObject1.put("categoryName",baseCategory1s.get(0).getCategory1Name());

            // 从结果集按照 baseCategory2 分组
            Map<Long, List<BaseCategoryView>> baseCategory2 = baseCategory1s.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 创建一个 jsonObject1 对象 的子集合对象
            List<JSONObject> categoryChild1 = new ArrayList();
            baseCategory2.forEach((category2Id,baseCategory2s) -> {

                JSONObject jsonObject2 = new JSONObject();
                // 添加 category2Id 数据
                jsonObject2.put("categoryId",category2Id);
                // 添加 categoryName 数据
                jsonObject2.put("categoryName",baseCategory2s.get(0).getCategory2Name());
                // 创建一个 jsonObject2 对象 的子集合对象
                List<JSONObject> categoryChild2 = new ArrayList();
                // 遍历baseCategory2s集合数据
                baseCategory2s.stream().forEach(baseCategoryView3 -> {
                    JSONObject jsonObject3 = new JSONObject();
                    // 添加 category3Id 数据
                    jsonObject3.put("categoryId",baseCategoryView3.getCategory3Id());
                    // 添加 categoryName 数据
                    jsonObject3.put("categoryName",baseCategoryView3.getCategory3Name());
                    categoryChild2.add(jsonObject3);
                });
                // 把categoryChild2数据添加的 jsonObject2
                jsonObject2.put("categoryChild",categoryChild2);
                // 添加数据到 categoryChild1 中
                categoryChild1.add(jsonObject2);
            });
            // 把categoryChild1数据添加的 jsonObject2
            jsonObject1.put("categoryChild",categoryChild1);
            // 添加数据到 jsonObjects 中
            jsonObjects.add(jsonObject1);

        } );

        return jsonObjects;
    }



}
