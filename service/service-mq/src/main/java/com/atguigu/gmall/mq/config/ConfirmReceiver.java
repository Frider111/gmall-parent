package com.atguigu.gmall.mq.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Blue Grass
 * @date 2020/9/9 - 21:16
 */
@Configuration
public class ConfirmReceiver {

//    @RabbitListener(queues = "testQueue")
//    public void process(Message message, Channel channel)
//    {
//        System.out.println("======================================");
////        System.out.println("message = " + message);
//        System.out.println("channel = " + channel);
//
//        System.out.println(message.getBody());
//
//    }

    @RabbitListener(bindings = @QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(name = "test1Q", durable = "true", autoDelete = "false"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(name = "exChange1"),
            key = "test11Q"
    ))
    public void process1(Message message, Channel channel) throws Exception {

        System.out.println("======================================");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        System.out.println("======" + new String(message.getBody()));

//     确认收到消息   channel.basicAck(deliveryTag,true);

        channel.basicNack(deliveryTag, true, false);


    }


//    @RabbitListener(bindings = @QueueBinding(
//            value = @org.springframework.amqp.rabbit.annotation.Queue(name = "test1Q1", durable = "true", autoDelete = "false"),
//            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(name = "exChange11"),
//            key = "test111Q"
//    ))
//    public void process2(Message message, Channel channel) throws Exception {
//
//        System.out.println("======================================");
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//
//        System.out.println("======" + new String(message.getBody()));
//
////     确认收到消息   channel.basicAck(deliveryTag,true);
//
//        channel.basicNack(deliveryTag, true, false);
//
//
//    }



//
//    @Bean("testQueue")
//    public Queue testQueue()
//    {
//        return new Queue("testQueue");
//    }
//
//    @Bean("testExchange")
//    public Exchange testExchange()
//    {
//        return new DirectExchange("testExchange");
//    }
//
//    @Bean("testBinding")
//    public Binding testBinding(@Qualifier("testQueue") Queue queue,
//                               @Qualifier("testExchange") Exchange exchange)
//    {
//        return BindingBuilder.bind(queue).to(exchange).with("testQueue").noargs() ;
//    }

    @Bean("testExchange111")
    public Exchange testExchange() {
        return new DirectExchange("testExchange111");
    }


}
