package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:25
 */
public interface UserAddressService {


    List<UserAddress> findUserAddressListByUserId(String userId);


}
