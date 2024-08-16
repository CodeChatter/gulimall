package com.xunqi.gulimall.order.controller;


import com.xunqi.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/mq-test")
public class MQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/createOrder")
    public String createOrder() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());

        // 给mq发送消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
        return "ok";
    }
}
