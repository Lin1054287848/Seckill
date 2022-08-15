package com.xxxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.xxxx.seckill.config.AccessLimit;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.pojo.*;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillGoodsService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀
 * 5000线程数 3次
 * windows: 优化前QPS： 2110 (超卖)
 * windows: 缓存QPS: 3379
 * windows: 优化QPS: 3489
 *
 * Linux 优化前QPS： 488.1(超卖)
 * 流程：
 * 1.判断用户是否登录
 * 2.判断秒杀库存够不够
 * 3.判断用户是否已经限购
 * 4.正式秒杀 下单
 *
 * 解决超卖：
 *  1.给数据库加唯一索引， 防止用户重复购买
 *  2.修改sql语句，增加了库存数量的判断，防止库存变成负数
 */
//静态页面优化后
@Slf4j
@Controller
@RequestMapping("/seckill")
public class secKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender; //RabbitMQ 生产者
    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /*
    @RequestMapping("/doSeckill2")
    public String doSeckill2(Model model, User user, Long goodsId){
        if(user == null){
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存是否足够
        if(goods.getStockCount() < 1){
            model.addAttribute("error", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }

        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
                eq("user_id", user.getId()).eq("goods_id",goodsId));
        if(seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());//该商品限购一件的错误
            return "secKillFail";
        }
        Order order = orderService.secKill(user, goods); //生成订单
        model.addAttribute("order", order); //订单详情
        model.addAttribute("goods", goods); //商品详情
        return "orderDetail";

    }
    */
    //秒杀的静态化优化
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //校验秒杀地址
        boolean check = orderService.checkPath(user, goodsId, path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);//通过键获取秒杀订单的值
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记，减少redis的访问
        if(EmptyStockMap.get(goodsId)){ //在进行预减库存前，对库存进行判断是否为空，如果为空，直接返回库存为空的错误，防止在库存为0的一瞬间大量的并发请求频繁的去操作redis
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存（decrement：原子性）
        //Long stock = valueOperations.decrement("seckillGoods:" + goodsId); //每调用一次 该商品库存减一
        //优化 采用lua脚本
        Long stock = (Long) redisTemplate.execute(script,Collections.singletonList("seckillGoods:"+goodsId),Collections.EMPTY_LIST);//优化 每调用一次 该商品库存减一
        if(stock < 0){ //每次调用库存都会 减一 ， 当stock（库存）<0时 就会报错
            EmptyStockMap.put(goodsId, true); //将该商品库存设为空
            valueOperations.increment("seckillGoods:" + goodsId); // 因为最后一次当是stock为0是 会再减一次 然后进入报错，对库存加1； -1 —> 0 使得库存为0；
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId); //获取秒杀订单信息
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage)); //将秒杀订单信息(将json数据转换成pojo对象list) 放入rabbitmq队列中
        log.info("将秒杀订单信息放入rabbitmq队列中:" + seckillMessage);
        return RespBean.success(0);




        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存是否足够
        if(goods.getStockCount() < 1){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //判断是否重复抢购
        //SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
                //eq("user_id", user.getId()).eq("goods_id",goodsId));
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);//通过键获取秒杀订单的值
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        Order order = orderService.seckill(user, goods); //生成订单
        return RespBean.success(order);
        */
    }


    /**
     * 获取秒杀结果
     */
    //获取秒杀结果
    //成功：orderId，失败：-1，排队中：0
    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (null == user) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 秒杀接口 优化： 获取秒杀地址
     * @throws Exception
     */
    @AccessLimit(second=5, maxCount=5, needLogin=true) //设置注解增强，限流 防止代码出现冗余
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //该部分采用注解增强 代替
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        //限制访问次数，5秒内访问5次 防止频繁访问
//        String uri = request.getRequestURI(); //URI统一资源标识符，它是一个字符串用来标示抽象或物理资源。（相对地址） //限流采用的是计数器算法
//        //更好的限流算法：滑动窗口（zset）、漏铜算法、令牌桶算法
//        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
//        if(count == null){
//            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS); //防止某个用户5秒内 超过五次访问 频繁访问的限制
//        }else if(count < 5){
//            valueOperations.increment(uri + ":" + user.getId());
//        }else{
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REAHCED);
//        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha); //判断验证码输入是否正确
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }


    @RequestMapping(value = "captcha", method = RequestMethod.GET)
    @ResponseBody
    public void verifyCode(User user, Long goodsId, HttpServletResponse rep) {
        if (null == user || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        rep.setContentType("image/jpg");
        rep.setHeader("Pargam", "No-cache");
        rep.setHeader("Cache-Control", "no-cache");
        rep.setDateHeader("Expires", 0);
        //生成验证码，放入Redis中
        //ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3); //算术验证码 （不能用）
        //动态验证码
        GifCaptcha captcha = new GifCaptcha(130, 32, 3);
        //静态验证码
        //SpecCaptcha captcha = new SpecCaptcha(130, 32, 3);
        //中文验证码
        //ChineseCaptcha captcha = new ChineseCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId,
                captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(rep.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }


    /**
     * 系统初始化； 把商品库存数量加载到Redis中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){ //若商品列表为空 直接返回
            return;
        }
        list.forEach(goodsVo ->{
                    //把列表全部存入redis  key :"seckillGoods:"+ 商品id  value: 商品的数量
                    redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
                    EmptyStockMap.put(goodsVo.getId(), false); //若该商品有库存 说明他的库存不为空
                });


    }
}
