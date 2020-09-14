package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import lombok.SneakyThrows;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Blue Grass
 * @date 2020/9/9 - 21:32
 */
@Service
public class RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate ;

    @Autowired
    RedisTemplate redisTemplate ;

    //过期时间：分钟
    public static final int OBJECT_TIMEOUT = 10;

    public void sendDelayMessage(String exchange,String routingKey ,Object message)
    {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();

        String correlationId = UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(message);
        gmallCorrelationData.setDelay(true);

        redisTemplate.opsForValue().set(correlationId, JSONObject.toJSONString(gmallCorrelationData),3,TimeUnit.MINUTES);

        rabbitTemplate.convertAndSend(exchange,routingKey,message,(message1) -> {
            // 设置延时时间
            message1.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime()*100000);
            return message1 ;
        },gmallCorrelationData);
    }

    public void sendMessage(String exchange,String routingKey ,Object message)
    {

        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();

        String correlationId = UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(message);

        redisTemplate.opsForValue().set(correlationId, JSONObject.toJSONString(gmallCorrelationData),OBJECT_TIMEOUT, TimeUnit.MINUTES);

        rabbitTemplate.convertAndSend(exchange, routingKey,message,gmallCorrelationData);

    }



}
