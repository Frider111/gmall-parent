package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author abt
 * @date 2020/8/18 - 15:34
 */
@ComponentScan(basePackages = {"com.atguigu.gmall"})
@SpringBootApplication
@MapperScan(basePackages = {"com.atguigu.gmall.product.mapper"})
public class ServiceProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class,args);
    }

}
