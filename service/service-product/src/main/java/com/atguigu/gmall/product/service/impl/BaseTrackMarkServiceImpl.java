package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.TrackMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 15:12
 */
@Service
public class BaseTrackMarkServiceImpl implements TrackMarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 查询品牌信息数据
     * @return
     */
    @Override
    public List<BaseTrademark> getTrademarkList() {

        return baseTrademarkMapper.selectList(null);

    }
}
