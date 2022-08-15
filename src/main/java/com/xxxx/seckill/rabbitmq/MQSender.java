package com.xxxx.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者
 */
@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //发送秒杀信息
    public void sendSeckillMessage(String message) {
        log.info("发送消息：" + message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);
    }

    //测试
//    public void send(Object msg){
//        log.info("发送消息:" + msg);
//        rabbitTemplate.convertAndSend("queue", msg); //将消息发送到RabbitMQ中的 queue队列中
//    }
//
//    /**
//     * 发送消息到交换机 Fanout模式 (广播模式）
//     */
//    public void send_fanout(Object msg){
//        log.info("发送消息:" + msg);
//        rabbitTemplate.convertAndSend("fanoutExchange", "", msg); //将消息发送到RabbitMQ中的 queue队列中
//    }
//
//    /**
//     * Direct模式 （筛选广播）
//     * 与Fanout模式的区别：多了一个路由key 通过不同的路由key 去 匹配不同的队列
//     */
//    public void send_direct01(Object msg){
//        log.info("发送red消息:" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg); //将消息发送到RabbitMQ中的 路由键为red的queue队列中
//    }
//    public void send_direct02(Object msg){
//        log.info("发送green消息:" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg); //将消息发送到RabbitMQ中的 路由键为green的queue队列中
//    }
//
//    /**
//     * Topic模式（不同模式筛选广播）
//     * 与Fanout模式的区别：多了一个路由key 通过不同的路由key 去 匹配不同的队列
//     */
//    public void send_topic01(Object msg){
//        log.info("发送消息(QUEUE1接收):" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg); //将消息发送到RabbitMQ中的 路由键为red的queue队列中
//    }
//    public void send_topic02(Object msg){
//        log.info("发送green消息(QUEUE1, QUEUE2接收):" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "message.queue.red.message", msg); //将消息发送到RabbitMQ中的 路由键为green的queue队列中
//    }
//
//    /**
//     * Header模式
//     *
//     */
//    public void send_header01(String msg){
//        log.info("发送消息(被两个QUEUE接收):" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "fast");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headerExchange", "", message); //将消息发送到RabbitMQ中的 map中"color"和"speed"的值对应的queue队列中
//    }
//    public void send_header02(String msg){
//        log.info("发送消息(被QUEUE1接收):" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "normal");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headerExchange", "", message); //将消息发送到RabbitMQ中的 map中"color"或"speed"的值对应的queue队列中
//    }
}
