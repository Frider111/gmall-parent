package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.service.ActivityService;
import com.atguigu.gmall.activity.service.impl.ActivityServiceImpl;
import com.atguigu.gmall.common.const1.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.user.UserRecode;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 11:11
 */
@Configuration
public class ActivityReceiver {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ActivityServiceImpl actyService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = MqConst.ROUTING_SECKILL_USER
    ))
    public void orderProcess(Message message, Channel channel, UserRecode messageRecodeSeckill) throws IOException {

        System.out.println("开始处理用户。。。");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            actyService.seckillOrder(messageRecodeSeckill.getSkuId(),messageRecodeSeckill.getUserId());
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            Boolean redelivered = message.getMessageProperties().getRedelivered();

            if (redelivered)
            {
                System.out.println("消息不退回了");
                channel.basicNack(deliveryTag,false,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = MqConst.ROUTING_TASK_1
    ))
    public void taskProcess(Message message, Channel channel) throws Exception
    {
        System.out.println("开始把数据从 mysql 服务器到redis当中");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            boolean isSuccess = actyService.pushRexdisStock();
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            Boolean redelivered = message.getMessageProperties().getRedelivered();

            if (redelivered)
            {
                System.out.println("消息不退回了");
                channel.basicNack(deliveryTag,false,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = MqConst.ROUTING_TASK_18
    ))
    public void deleteRedisSeckill(Message message, Channel channel) throws Exception
    {
        System.out.println("开始把 redis 数据删除，啦啦啦啦");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            boolean isSuccess = actyService.deleteRedisSeckill();
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            Boolean redelivered = message.getMessageProperties().getRedelivered();

            if (redelivered)
            {
                System.out.println("消息不退回了");
                channel.basicNack(deliveryTag,false,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }
    }


}
