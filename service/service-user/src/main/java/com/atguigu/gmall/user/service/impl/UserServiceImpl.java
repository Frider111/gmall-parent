package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 14:55
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper ;

    @Autowired
    RedisTemplate redisTemplate ;

    @Override
    public Map login(UserInfo userInfo) {

        Map userInfoMap = new HashMap();

        String loginName = userInfo.getLoginName();
        // md5 密码解密
        String passwd = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        userInfoQueryWrapper.eq("login_name", loginName);
        userInfoQueryWrapper.eq("passwd", passwd);

        UserInfo userInfo1 = userMapper.selectOne(userInfoQueryWrapper);

        if (userInfo1!=null){
            String token = UUID.randomUUID().toString();
            userInfoMap.put("userInfo",userInfo1);
            userInfoMap.put("token",token);
            // 把 token 数据放入 redis 当中
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,
                    userInfo1.getId(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
        }

        return userInfoMap;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+token);
    }

}
