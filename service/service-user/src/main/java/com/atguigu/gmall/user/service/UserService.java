package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 14:53
 */
public interface UserService {

    Map login(UserInfo userInfo);

    void logout(String token);
}
