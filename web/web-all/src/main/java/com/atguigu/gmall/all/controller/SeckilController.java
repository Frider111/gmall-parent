package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.SeckilFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 14:22
 */
@Controller
public class SeckilController {

    @Autowired
    SeckilFeignClient seckilFeignClient;

    @Autowired
    SpringTemplateEngine springTemplateEngine;

    @RequestMapping("seckill.html")
    public String index(Model model) {

        Result result = seckilFeignClient.findAll();

        model.addAttribute("list", result.getData());

        return "seckill/index";
    }


    @GetMapping("seckill/{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) throws IOException {
        // 通过skuId 查询skuInfo


        String path = this.getClass().getClassLoader().getResource("static").getPath();

        File file = new File(path, "seckill1");

        if (!file.exists()) {
            file.mkdir();
        }

        String urlName = "item" + skuId + ".html";

        File isStore = new File(file, urlName);

        if (!isStore.exists()) {
            FileWriter fw = new FileWriter(isStore);
            Result result = seckilFeignClient.getSeckillGoods(skuId);
//        model.addAttribute("item", result.getData());
            Context context = new Context();

            context.setVariable("item", result.getData());

            springTemplateEngine.process("seckill/item", context, fw);
        }

        return "redirect:http://activity.gmall.com/seckill1/" + urlName;
    }


    /**
     * 秒杀排队
     *
     * @param skuId
     * @param skuIdStr
     * @param request
     * @return
     */
    @GetMapping("seckill/queue.html")
    public String queue(@RequestParam(name = "skuId") Long skuId,
                        @RequestParam(name = "skuIdStr") String skuIdStr,
                        HttpServletRequest request) {

        boolean isStore = seckilFeignClient.checkSkuIdStr(skuIdStr);

        if (!isStore) {
            String referer = request.getHeader("Referer");
            return "redirect:http://activity.gmall.com/seckill/" + skuId + ".html";
        }
        request.setAttribute("skuId", skuId);
        request.setAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    @GetMapping("seckill/trade.html")
    public String seckillTrade(Model model) {

        Result<Map<String, Object>> result = seckilFeignClient.trade();
        model.addAllAttributes(result.getData());
        return "seckill/trade";

    }
}
