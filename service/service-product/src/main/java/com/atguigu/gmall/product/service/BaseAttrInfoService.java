package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;

import java.nio.file.LinkOption;
import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 9:35
 */
public interface BaseAttrInfoService {

    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id,Long category3Id);

    boolean insert(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long attrId);
}
