package com.xxxx.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigDirect {
    private static final String QUEUE01 = "queue_direct01"; //队列
    private static final String QUEUE02 = "queue_direct02";
    private static final String EXCHANGE = "directExchange"; //交换机
    private static final String ROUTINGKEY01 = "queue.red"; //路由键， 不同的路由键 将会被分配到不同的队列中去
    private static final String ROUTINGKEY02 = "queue.green";

    @Bean
    public Queue queue_direct01(){
        return new Queue(QUEUE01); //创建一个名为QUEUE01的队列
    }

    @Bean
    public Queue queue_direct02(){
        return new Queue(QUEUE02); //创建一个名为QUEUE02的队列
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(EXCHANGE); //创建一个名为EXCHANGE的交换机
    }

    //将队列queue01绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_direct01(){
        return BindingBuilder.bind(queue_direct01()).to(directExchange()).with(ROUTINGKEY01); //队列绑定交换机， 并加上路由键ROUTINGKEY01
    }

    //将队列queue02绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_direct02(){
        return BindingBuilder.bind(queue_direct02()).to(directExchange()).with(ROUTINGKEY02); //队列绑定交换机， 并加上路由键ROUTINGKEY02
    }
}
