package com.xxxx.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfigHeader {

    private static final String QUEUE01 = "queue_header01"; //队列
    private static final String QUEUE02 = "queue_header02";
    private static final String EXCHANGE = "headerExchange"; //交换机

    @Bean
    public Queue queue_header01(){
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queue_header02(){
        return new Queue(QUEUE02);
    }

    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(EXCHANGE);
    }

    @Bean
    public Binding binding_header01(){
        Map<String, Object> map = new HashMap<>();
        map.put("color", "red");
        map.put("speed", "low");
        return BindingBuilder.bind(queue_header01()).to(headersExchange()).whereAny(map).match();//map中的键任意匹配到一个"color"和"speed"的值  就能转发到对应的队列
    }

    @Bean
    public Binding binding_header02(){
        Map<String, Object> map = new HashMap<>();
        map.put("color", "red");
        map.put("speed", "fast");
        return BindingBuilder.bind(queue_header02()).to(headersExchange()).whereAll(map).match(); //map中的键同时匹配到"color"和"speed"的值 才能转发到对应的队列
    }

}
