package com.xxxx.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfigFanout {

    private static final String QUEUE01 = "queue_fanout01";
    private static final String QUEUE02 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange"; //交换机

    @Bean
    public Queue queue(){
        return new Queue("queue", true); //创建一个名为queue的队列 ，true代表队列持久化
    }

    @Bean
    public Queue queue_fanout01(){
        return new Queue(QUEUE01); //创建一个名为QUEUE01的队列
    }

    @Bean
    public Queue queue_fanout02(){
        return new Queue(QUEUE02); //创建一个名为QUEUE02的队列
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(EXCHANGE); //创建一个名为EXCHANGE的交换机
    }

    //将队列queue01绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_fanout01(){
        return BindingBuilder.bind(queue_fanout01()).to(fanoutExchange());
    }

    //将队列queue02绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_fanout02(){
        return BindingBuilder.bind(queue_fanout02()).to(fanoutExchange());
    }
}
