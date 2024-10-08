package com.xunqi.gulimall.order.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;


@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就会回调
     *      1、spring.rabbitmq.publisher-confirms: true
     *      2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     *      1、spring.rabbitmq.publisher-returns: true
     *         spring.rabbitmq.template.mandatory: true
     *      2、设置确认回调ReturnCallback
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     *      # 手动ack消息，不使用默认的消费端确认
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动签收
     *      1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：
     *              我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了。发生了消息丢失
     *              消费者手动确认模式。只要我们没有明确告诉MQ，货物被签收。没有被Ack，消息就一直是unack状态。
     *              即使Consumer宕机。消息不会丢失，会重新变为Ready，下一次有新的Consumer连接进来就发给他
     *      2、如何签收：
     *          channel.basicAck(deliveryTag, false);   签收：业务成功完成就应该签收
     *          channel.basicNack(deliveryTag, false, false);   拒签：业务失败就应该拒签
     *
     *
     */
    @PostConstruct  //MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {

        /**
         * 1、只要消息抵达Broker就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        // 设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            /**
             * 防止消息丢失
             *
             * 1、做好消息确认机制（publiser、consumer【手动ack】）
             * 2、每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送
             */

            // 服务器收到了
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
        });


        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         */
        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            // TODO：消息丢失处理：报错了。修改数据库当前消息的状态-》错误，方便后期处理
            System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]" +
                    "==>replyText["+replyText+"]==>exchange["+exchange+"]==>routingKey["+routingKey+"]");
        });
    }
}
