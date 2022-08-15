package com.xxxx.seckill.rabbitmq;

import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息消费者
 */
@Service
@Slf4j
public class MQReceiver {


    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receive(String msg) {
        log.info("接收消息：" + msg);
        //下单操作
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(msg, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        //判断库存
        GoodsVo goodsVoByGoodsId = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVoByGoodsId.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return;
        }
        //下单操作
        orderService.seckill(user, goodsVoByGoodsId);
    }

//    @RabbitListener(queues = "queue") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive(Object msg){
//        log.info("接收消息:" + msg); //消费消息
//    }
//
//    /**
//     * Fanout模式
//     * @param msg
//     */
//    @RabbitListener(queues = "queue_fanout01") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_fanout01(Object msg){
//        log.info("QUEUE01接收消息:" + msg); //消费消息
//    }
//    @RabbitListener(queues = "queue_fanout02") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_fanout02(Object msg){
//        log.info("QUEUE02接收消息:" + msg); //消费消息
//    }
//
//    /**
//     * Direct模式
//     */
//    @RabbitListener(queues = "queue_direct01") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_direct01(Object msg){
//        log.info("QUEUE01接收消息:" + msg); //消费消息
//    }
//    @RabbitListener(queues = "queue_direct02") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_direct02(Object msg){
//        log.info("QUEUE02接收消息:" + msg); //消费消息
//    }
//
//    /**
//     * Topic模式
//     */
//    @RabbitListener(queues = "queue_topic01") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_topic01(Object msg){
//        log.info("QUEUE01接收消息:" + msg); //消费消息
//    }
//    @RabbitListener(queues = "queue_topic02") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_topic02(Object msg){
//        log.info("QUEUE02接收消息:" + msg); //消费消息
//    }
//
//    /**
//     * Header模式
//     */
//    @RabbitListener(queues = "queue_header01") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_header01(Message msg){
//        log.info("QUEUE01接收Message对象:" + msg); //消费消息
//        log.info("QUEUE01接收消息:" + new String(msg.getBody())); //消费消息
//    }
//    @RabbitListener(queues = "queue_header02") //设置消息的消费者监听 queue队列（消息发送者发送到的队列）
//    public void receive_header02(Message msg){
//        log.info("QUEUE02接收Message对象:" + msg); //消费消息
//        log.info("QUEUE02接收消息:" + new String(msg.getBody())); //消费消息
//    }
}
