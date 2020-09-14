package com.atguigu.gmall.activity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 10:46
 */
@SpringBootApplication
@ComponentScan("com.atguigu.gmall")
@EnableFeignClients("com.atguigu.gmall")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gmall")
public class ActivityApplication {

    public static void main(String[] args) {

        SpringApplication.run(ActivityApplication.class, args) ;
    }

}

