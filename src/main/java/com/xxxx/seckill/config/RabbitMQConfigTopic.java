package com.xxxx.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigTopic {

    private static final String QUEUE = "seckillQueue"; //队列
    private static final String EXCHANGE = "seckillExchange"; //交换机

    @Bean
    public Queue queue_topic() {
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding_topic() {
        return BindingBuilder.bind(queue_topic()).to(topicExchange()).with("seckill.#");
    }








    //测试
    /*private static final String QUEUE01 = "queue_topic01"; //队列
    private static final String QUEUE02 = "queue_topic02";
    private static final String EXCHANGE = "topicExchange"; //交换机
    private static final String ROUTINGKEY01 = "#.queue.#"; //设置主题键
    private static final String ROUTINGKEY02 = "*.queue.#";//设置主题键

    @Bean
    public Queue queue_topic01(){
        return new Queue(QUEUE01); //创建一个名为QUEUE01的队列
    }

    @Bean
    public Queue queue_topic02(){
        return new Queue(QUEUE02); //创建一个名为QUEUE02的队列
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE); //创建一个名为EXCHANGE的交换机
    }

    //将队列queue01绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_topic01(){
        return BindingBuilder.bind(queue_topic01()).to(topicExchange()).with(ROUTINGKEY01); //队列绑定交换机， 并加上路由键ROUTINGKEY01
    }

    //将队列queue02绑定到交换机fanoutExchange上
    @Bean
    public Binding binding_topic02(){
        return BindingBuilder.bind(queue_topic02()).to(topicExchange()).with(ROUTINGKEY02); //队列绑定交换机， 并加上路由键ROUTINGKEY02
    }*/
}
