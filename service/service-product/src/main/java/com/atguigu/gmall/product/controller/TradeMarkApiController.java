package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TrackMarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 前端访问地址 ：http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
     * @param page  表示当前页数和
     * @param limit 表示当前页显示条数
     * @return
     */
    @GetMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Long page,
                          @PathVariable("limit") Long limit){
        // 封装一个page对象
        Page<BaseTrademark> page1 = new Page<>(page, limit);
        // 对分页对象进行设置结果集
        trackMarkService.baseTrademark(page1);
        return Result.ok(page1);
    }

}
