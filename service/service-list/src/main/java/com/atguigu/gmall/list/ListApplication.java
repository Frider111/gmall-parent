package com.atguigu.gmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Blue Grass
 * @date 2020/8/30 - 18:54
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class} )
@ComponentScan(basePackages={"com.atguigu.gmall"})
@EnableFeignClients(basePackages={"com.atguigu.gmall"})
@EnableDiscoveryClient
public class ListApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListApplication.class, args);
    }

}
