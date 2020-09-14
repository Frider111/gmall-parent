package com.atguigu.gmall.task.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.const1.MqConst;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Blue Grass
 * @date 2020/9/11 - 18:48
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitService rabbitService ;

    @Scheduled(cron = "0/30 * * * * ? ")
    public void task() {

        for (int i = 0; i < redisTemplate.opsForList().size(MqConst.MQ_KEY_PREFIX); i++) {

            String msg = (String) redisTemplate.opsForList().rightPop(MqConst.MQ_KEY_PREFIX);

            if (msg != null) {
                GmallCorrelationData gmallCorrelationData = JSONObject.parseObject(msg, GmallCorrelationData.class);

                if (gmallCorrelationData.isDelay()){
                    rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),gmallCorrelationData.getMessage(),message ->{
                        message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime()*1000);
                        return message;
                    },gmallCorrelationData);
                }
                else
                {
                    rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), gmallCorrelationData.getRoutingKey(),
                            gmallCorrelationData.getMessage(), gmallCorrelationData);
                }

            }
        }
    }

    // 定时任务，把秒杀库的数据推送到redis当中
    @Scheduled(cron = "0/30 * * * * ? ")
    public void activityTask()
    {
        System.out.println("定时任务开启，起飞起飞");
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "");
    }

    // 定时任务，把秒杀库的数据推送到redis当中
    @Scheduled(cron = "0/30 * * * * ? ")
    public void deleteRedisSeckill()
    {
        System.out.println("删除redis数据，下落下落");
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_18, "");
    }


}
