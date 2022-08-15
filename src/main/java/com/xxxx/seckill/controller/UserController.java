package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zwh
 * @since 2022-07-26
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    /**
     * 用户信息（测试）
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){ //查看当前用户的信息
        return RespBean.success(user);
    }


//    /**
//     * 测试发送RabbitMQ消息 queue
//     */
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("Hello");
//    }
//
//    /**
//     * 测试发送RabbitMQ消息 Fanout模式
//     */
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mqFanout(){
//        mqSender.send_fanout("Hello");
//    }
//
//    /**
//     * 测试发送RabbitMQ消息 Direct模式
//     */
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void mqDirect01(){
//        mqSender.send_direct01("Hello, Red"); //发送路由键为Red的消息
//    }
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void mqDirect02(){
//        mqSender.send_direct02("Hello, Green"); //发送路由键为Green的消息
//    }
//
//    /**
//     * 测试发送RabbitMQ消息 Topic模式
//     */
//    @RequestMapping("/mq/topic01")
//    @ResponseBody
//    public void mqTopic01(){
//        mqSender.send_topic01("Hello, Red"); //发送路由键为Red的消息
//    }
//    @RequestMapping("/mq/topic02")
//    @ResponseBody
//    public void mqTopic02(){
//        mqSender.send_topic02("Hello, Green"); //发送路由键为Green的消息
//    }
//
//    /**
//     * 测试发送RabbitMQ消息 Header模式
//     * 根据map中的键所对应的值 通过 All或Any 来进行匹配
//     */
//    @RequestMapping("/mq/header01")
//    @ResponseBody
//    public void mqHeader01(){
//        mqSender.send_header01("Hello, Header01"); //发送路由键为Red的消息
//    }
//    @RequestMapping("/mq/header02")
//    @ResponseBody
//    public void mqHeader02(){
//        mqSender.send_header02("Hello, Header02"); //发送路由键为Green的消息
//    }
}
