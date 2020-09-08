package com.atguigu.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/9/2 - 14:51
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.atguigu.gmall"})
@ComponentScan(basePackages = {"com.atguigu.gmall"})
public class GatewayApplication {

    public static void main(String[] args) {

        SpringApplication.run(GatewayApplication.class,args);
    }

}
