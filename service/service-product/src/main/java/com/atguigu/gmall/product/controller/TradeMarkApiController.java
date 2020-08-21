package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TrackMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author abt
 * @date 2020/8/19 - 15:09
 */
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class TradeMarkApiController {



    @Autowired
    private TrackMarkService trackMarkService;

    /**
     * 请求地址 ：http://api.gmall.com/admin/product/baseTrademark/getTrademarkList
     * 获取品牌信息
     * @return
     */
    @GetMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList(){

        List<BaseTrademark> baseTrademarks = trackMarkService.getTrademarkList();
        return Result.ok(baseTrademarks);
    }


}
