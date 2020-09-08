package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:25
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressMapper userAddressMapper ;

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {

        if (StringUtils.isBlank(userId))
        {
            return null ;
        }

        QueryWrapper<UserAddress> userAddressQueryWrapper = new QueryWrapper<>();

        userAddressQueryWrapper.eq("user_id", userId);

        List<UserAddress> userAddresses = userAddressMapper.selectList(userAddressQueryWrapper);

        return userAddresses;
    }
}
