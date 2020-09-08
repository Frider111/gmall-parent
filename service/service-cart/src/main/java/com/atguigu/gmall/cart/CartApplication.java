package com.atguigu.gmall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/9/4 - 20:51
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.atguigu.gmall"})
@MapperScan(basePackages = {"com.atguigu.gmall"})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atguigu.gmall"})
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class,args);
    }
}
