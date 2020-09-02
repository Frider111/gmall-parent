package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 21:04
 */

public interface ListService {

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    void hotScore(Long skuId);

    SearchResponseVo list(SearchParam searchParam) throws IOException;
}
