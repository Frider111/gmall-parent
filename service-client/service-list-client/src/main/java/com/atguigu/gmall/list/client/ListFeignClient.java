package com.atguigu.gmall.list.client;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 20:55
 */
@FeignClient(value = "SERVICE-LIST")
public interface ListFeignClient {

    @RequestMapping("api/list/inner/cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/inner/onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/inner/hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId) ;


    @PostMapping("api/list/inner/list")
    Result list(@RequestBody SearchParam searchParam) throws IOException;

}
