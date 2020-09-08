package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 20:17
 */
@Controller
public class PassportController {

    @RequestMapping("login.html")
    public String toLogin(String originUrl, Model model){

        model.addAttribute("originUrl",originUrl);
        return "login" ;
    }

}
