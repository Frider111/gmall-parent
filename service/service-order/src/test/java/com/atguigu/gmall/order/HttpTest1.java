package com.atguigu.gmall.order;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.HttpClientUtil;
import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/9/8 - 15:08
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HttpTest1 {

    @Autowired
    RedisTemplate redisTemplate ;

    @Test
    public void test1(){
        boolean isExist = redisTemplate.opsForValue().setIfAbsent("1111","12");
        System.out.println("isExist = " + isExist);
    }

}
