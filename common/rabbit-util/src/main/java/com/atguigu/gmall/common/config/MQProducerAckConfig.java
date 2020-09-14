package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.const1.MqConst;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Blue Grass
 * @date 2020/9/10 - 15:09
 */
@Component
public class MQProducerAckConfig implements RabbitTemplate.ReturnCallback, RabbitTemplate.ConfirmCallback {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate ;

    @PostConstruct()
    public void init() {

        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setMandatory(true);

    }

    /*
           第一步：调用setConfirmCallback方法。
           第二步：匿名内部类实现接口 RabbitTemplate.ConfirmCallback
           第三步：ack代表着消息返回是否成功 true代表成功
           第四步：交换机如果找不到，就会返回失败
           第五步：如果交换机正确，队列不正确【也是返回成功【因为他只判断能不能给交换机发送消息，难受】】
            */

    /**
     * @param correlationData 交换机的名字
     * @param ack             否发送成功
     * @param cause           失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData)correlationData;
        if (ack) {
            System.out.println("消息发送成功"+ JSONObject.toJSONString(correlationData));
            redisTemplate.delete(gmallCorrelationData.getId());
        }
        else {
            System.out.println("消息发送失败，数据"+ JSONObject.toJSONString(correlationData)+",原因："+cause);
            this.addRetry(correlationData) ;
        }
    }

    /**
     * 第一步：开启return回退机制
     * <p>
     * 第二步:调用setReturnCallback方法。
     * 第三步：匿名内部类实现接口 new RabbitTemplate.ReturnCallback()
     * 第四步：当交换机给转发消息的时候，如果路由规则写错了，是没有办法转发给队列。
     * 第一种处理，系统会直接丢掉消息
     * 第二种处理，强制发送【需要设置】 rabbitTemplate.setMandatory(true);
     * 第五步：队列找不到就会失败.
     *
     * @param message    :表示消息对象
     * @param replyCode  ：表示错误码
     * @param replyText  ：表示报错信息
     * @param exchange   ：表示交换机
     * @param routingKey ：表示路由贵族
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

        System.out.println("当前发送的消息 = " + message);
        System.out.println("问题的错误码= " + replyCode);
        System.out.println("当前使用的交换机 = " + exchange);
        System.out.println("当前使用的路由Key = " + routingKey);

    }

    /**
     * 消息重试方法
     * @param correlationData
     */
    private void addRetry(CorrelationData correlationData)
    {
        System.out.println("correlationData = " + correlationData);
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData)correlationData ;

        if (gmallCorrelationData.getRetryCount() > MqConst.RETRY_COUNT)
        {
            System.out.println(("消息重试失败"+ JSONObject.toJSONString(correlationData)));
        }
        else
        {
            gmallCorrelationData.setRetryCount(gmallCorrelationData.getRetryCount() + 1 );
            redisTemplate.opsForList().rightPush(MqConst.MQ_KEY_PREFIX, JSONObject.toJSONString(gmallCorrelationData)) ;
            redisTemplate.opsForValue().set(correlationData.getId(), JSONObject.toJSONString(gmallCorrelationData));
        }
    }

}
