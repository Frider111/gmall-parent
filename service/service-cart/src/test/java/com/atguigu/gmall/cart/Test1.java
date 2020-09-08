package com.atguigu.gmall.cart;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.service.impl.CartInfoServiceImpl;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Blue Grass
 * @date 2020/9/5 - 21:40
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test1 {

    @Autowired
    CartInfoServiceImpl cartInfoService ;

    @Autowired
    CartInfoMapper cartInfoMapper ;

    @Test
    public void test1(){

        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();

        cartInfoQueryWrapper.eq("sku_id", "1")
                .eq("user_id", "1");


        QueryWrapper<CartInfo> cartInfoQueryWrapper1 = new QueryWrapper<>();

        cartInfoQueryWrapper1.eq("sku_id", "1");
        cartInfoQueryWrapper1.eq("user_Id", "1");

//        this.update(cartInfo, cartInfoQueryWrapper1) ;

        List<CartInfo> list = cartInfoMapper.selectList(cartInfoQueryWrapper);
        List<CartInfo> list1 = cartInfoMapper.selectList(cartInfoQueryWrapper1);



        System.out.println("cartInfoQueryWrapper1 = " + cartInfoQueryWrapper1);
        
    }

    @Test
    public void test2(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


        System.out.println(simpleDateFormat.format(new Date(System.currentTimeMillis()+60*60*24*1000)));
        System.out.println(simpleDateFormat.format(new Date()));

        Object o = null ;

        String str = (String)o;
        String str1 = o.toString() ;

    }
    

}
