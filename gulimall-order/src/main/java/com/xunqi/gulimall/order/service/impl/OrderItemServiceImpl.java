package com.xunqi.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.xunqi.common.utils.PageUtils;
import com.xunqi.common.utils.Query;
import com.xunqi.gulimall.order.dao.OrderItemDao;
import com.xunqi.gulimall.order.entity.OrderEntity;
import com.xunqi.gulimall.order.entity.OrderItemEntity;
import com.xunqi.gulimall.order.entity.OrderReturnReasonEntity;
import com.xunqi.gulimall.order.service.OrderItemService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;


//@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * queues：声明需要监听的队列
     * org.springframework.amqp.core.Message
     * <p>
     * 参数可以写一下类型
     * 1、Message message：原生消息详细信息，头+体
     * 2、T《发送消息的类型》OrderReturnReasonEntity contents；
     * <p>
     * Queue：可以很多人都来监听，只要收到消息，只能有一个客户端收到
     * 场景：
     * 1）订单服务启动多个；同一个消息只能有一个客户端收到
     * 2）只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     * <p>
     * channel：当前传输数据的通道
     */
    //@RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void recieveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {
        //拿到主体内容
        byte[] body = message.getBody();
        //拿到的消息头属性信息
        MessageProperties properties = message.getMessageProperties();
        System.out.println("接受到的消息...内容" + message + "===内容：" + content);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 签收货物，非批量模式
        try {
            if (deliveryTag % 2 == 0) {
                // 收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物..." + deliveryTag);
            } else {
                // 退货 requeue=false 丢弃 requeue=true 发回服务器，服务器重新入队
                // long deliveryTag，boolean multiple，boolean requeue
                channel.basicNack(deliveryTag, false, false);
                // channel.basicReject();
                System.out.println("没有签收货物..." + deliveryTag);
            }
        } catch (Exception e) {
            // 网络中断

        }
    }


    public void receiveMessage2(OrderEntity content) {

    }

}