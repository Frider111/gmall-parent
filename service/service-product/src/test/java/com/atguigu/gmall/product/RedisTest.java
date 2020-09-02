package com.atguigu.gmall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Blue Grass
 * @date 2020/8/24 - 11:29
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
//
//    @Autowired
//    private TestService testService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor ;

    @Test
    public void test(){
        System.out.println("threadPoolExecutor = " + threadPoolExecutor);
    }



}
