package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 19:57
 */
@RestController
public class AuthController {

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("api/user/inner/verify/{token}")
    public String verify(@PathVariable("token") String token) {

        Object userId = redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + token);
        return userId!=null?userId+"":null ;
    }

}
