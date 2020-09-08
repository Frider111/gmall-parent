package com.atguigu.gmall.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 19:56
 */
@ComponentScan(basePackages = {"com.atguigu.gmall"})
@SpringBootApplication
@MapperScan(basePackages = {"com.atguigu.gmall.user.mapper"})
@EnableDiscoveryClient
public class UserApplication {
    public static void main(String[] args)
    {

        SpringApplication.run(UserApplication.class,args);
    }
}
