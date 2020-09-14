package com.atguigu.gmall.activity.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author Blue Grass
 * @date 2020/9/13 - 13:54
 */
@Configuration
public class RedisChannelConfig {


    /**
     * 设置消息适配器
     * 设置 redis 连接工厂
     * 返回一个订阅对象
     * @param container
     * @param messageListener
     * @return
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory container, MessageListenerAdapter listenerAdapter)
    {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        //
        redisMessageListenerContainer.setConnectionFactory(container);
        // 设置接收频道通配符
        redisMessageListenerContainer.addMessageListener(listenerAdapter,new PatternTopic("seckillpush"));

        return redisMessageListenerContainer;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(MessageReceive receiver)
    {
        //这个地方 是给 messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receiveMessage”
        //也有好几个重载方法，这边默认调用处理器的方法 叫handleMessage 可以自己到源码里面看

        return new MessageListenerAdapter(receiver, "receiveMessage");
    }



    @Bean //注入操作数据的template
    public StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }


}
