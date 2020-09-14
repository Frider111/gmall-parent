package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.impl.OrderServiceImpl;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


/**
 * @author Blue Grass
 * @date 2020/9/11 - 11:02
 */
@Configuration
public class OrderReceiver {

    @Autowired
    OrderServiceImpl orderServiceImpl ;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = MqConst.ROUTING_PAYMENT_PAY
    ))
    public void process(Message message, Channel channel,String orderId) throws Exception
    {
        Long deliveryTag = message.getMessageProperties().getDeliveryTag() ;
        try {

            boolean isSuccess = orderServiceImpl.updateOrderInfo(Long.parseLong(orderId));

            if (isSuccess){
                orderServiceImpl.sendOrderStatus(orderId);
                channel.basicAck(deliveryTag,false);
            }
        }catch (Exception e){
            Boolean redelivered = message.getMessageProperties().getRedelivered();
            if (redelivered)
            {
                // 暂时就接收了吧
                channel.basicAck(deliveryTag,false) ;
            }
            channel.basicNack(deliveryTag,false,true) ;
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = MqConst.ROUTING_WARE_ORDER
    ))
    public void process1(Message message, Channel channel,String mapStr) throws Exception
    {
        Long deliveryTag = message.getMessageProperties().getDeliveryTag() ;
        try {

            Map map = JSONObject.parseObject(mapStr, Map.class);

            Object orderId = map.get("orderId");
            Object status = map.get("status");
            orderServiceImpl.updateOrderStatus(Long.valueOf(orderId.toString()), ProcessStatus.DELEVERED);


        }catch (Exception e){
            Boolean redelivered = message.getMessageProperties().getRedelivered();
            if (redelivered)
            {
                // 暂时就接收了吧
                channel.basicAck(deliveryTag,false) ;
            }
            channel.basicNack(deliveryTag,false,true) ;
        }
    }

}
