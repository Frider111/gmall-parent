package com.atguigu.gmall.product.common.thread;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author Blue Grass
 * @date 2020/8/27 - 10:31
 */
@Configuration
public class GmallThreadPool {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(50, 150, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue(10000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

}


