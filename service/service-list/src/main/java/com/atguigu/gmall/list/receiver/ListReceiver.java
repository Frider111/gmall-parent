package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.const1.MqConst;
import com.atguigu.gmall.list.service.ListService;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Blue Grass
 * @date 2020/9/11 - 19:52
 */
@Configuration
public class ListReceiver {

    @Autowired
    ListService listService ;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,declare = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = MqConst.ROUTING_GOODS_UPPER
    ))
    public void onSaleProcess(Message message , Channel channel,String skuId) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (StringUtils.isNotBlank(skuId))
            {
                listService.onSale(Long.valueOf(skuId));
                channel.basicAck(deliveryTag,false);
            }

        }catch (Exception e)
        {
            Boolean redelivered = message.getMessageProperties().getRedelivered();
            if (redelivered)
            {
                channel.basicAck(deliveryTag,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,declare = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = MqConst.ROUTING_GOODS_LOWER
    ))
    public void cancelSaleProcess(Message message , Channel channel,String skuId) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {

            if (StringUtils.isNotBlank(skuId))
            {
                listService.cancelSale(Long.valueOf(skuId));
                channel.basicAck(deliveryTag,false);
            }

        }catch (Exception e)
        {
            Boolean redelivered = message.getMessageProperties().getRedelivered();
            if (redelivered)
            {
                channel.basicAck(deliveryTag,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }
    }
}
