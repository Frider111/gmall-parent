package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Blue Grass
 * @date 2020/9/11 - 23:24
 */
@Configuration
public class OrderCanelMqConfig {

    @Autowired
    OrderService orderService ;

    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void processOrderCanel(Long orderId, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            System.out.println("orderId = " + orderId);
            OrderInfo orderInfo = orderService.getOrderInfo(orderId);
            if (!orderInfo.getOrderStatus().equals(OrderStatus.PAID))
            {
                orderService.execExpiredOrder(orderId);
                channel.basicAck(deliveryTag,false);
            }
        }catch (Exception e)
        {
            if (message.getMessageProperties().getRedelivered())
            {
                channel.basicAck(deliveryTag,false);
            }
            channel.basicNack(deliveryTag,false,true);
        }
    }


    @Bean
    public Queue delayQueue() {

        return new Queue(MqConst.QUEUE_ORDER_CANCEL);
    }

    @Bean
    public CustomExchange delayExchange() {

        Map<String, Object> delayMap = new HashMap<>();
        delayMap.put("x-delayed-type", "direct");
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, "x-delayed-message", true, false, delayMap);

    }

    @Bean
    public Binding delayBinding() {

        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();

    }

}
