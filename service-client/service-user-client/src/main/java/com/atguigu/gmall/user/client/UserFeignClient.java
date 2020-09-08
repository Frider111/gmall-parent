package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 19:52
 */
@FeignClient("SERVICE-USER")
public interface UserFeignClient {

    @RequestMapping("api/user/inner/verify/{token}")
    String verify(@PathVariable("token") String token);


    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable(value = "userId") String userId);



}
