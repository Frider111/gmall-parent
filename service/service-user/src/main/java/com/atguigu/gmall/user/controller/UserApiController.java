package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:23
 */
@RestController
@RequestMapping("api/user")
public class UserApiController {

    @Autowired
    UserAddressService userAddressService ;

    @GetMapping("inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable(value = "userId") String userId){

        List<UserAddress> userAddresses = userAddressService.findUserAddressListByUserId(userId);
        return userAddresses ;
    }
}
