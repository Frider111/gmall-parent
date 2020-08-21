package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 15:11
 */
public interface TrackMarkService {

    List<BaseTrademark> getTrademarkList();

    void baseTrademark(Page<BaseTrademark> page1);
}
