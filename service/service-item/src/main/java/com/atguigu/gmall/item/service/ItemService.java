package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/8/23 - 13:30
 */
public interface ItemService {


    Map<String, Object> getBySkuId(Long skuId) throws Exception;




}
