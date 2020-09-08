package com.atguigu.gmall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/9/7 - 14:12
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.atguigu.gmall")
@MapperScan("com.atguigu.gmall.order.mapper")
@EnableFeignClients("com.atguigu.gmall")
public class OrderApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderApplication.class,args) ;

    }

}
