package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.DetailVo;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver; //页面手动渲染
    /**
     * 功能描述：跳转到商品列表页
     * 5000线程数 3次
     * windows: 优化前QPS： 1579.9
     *          优化后QPS(redis：页面缓存)2143.8
     * Linux 优化前QPS： 478.3
     */
    @RequestMapping(value = "/toList", produces = "text/html;cjarset=utf-8")
    @ResponseBody
    public String toList( Model model, User user,  HttpServletRequest request, HttpServletResponse response){ //通过注解获取cookie值
        //Redis中获取页面，如果不为空，直接返回页面
        //页面缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            //直接返回页面
            return html;
        }
        model.addAttribute("user", user); //将用户信息传入model中 进而视图渲染
        model.addAttribute("goodsList", goodsService.findGoodsVo()); //将商品列表信息传入model中 进而视图渲染
        //return "goodsList";
        //如果为空，手动渲染，页面存入redis并返回
        model.addAttribute("user", user);
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        model.addAttribute("goodsList", goodsVo);
        WebContext webContext = new WebContext(request, response,
                request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 功能描述：跳转商品详情页面
     */
    /*
    //url缓存优化前
    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable Long goodsId){
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endData = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int secKillStatus = 0;
        int remainSeconds = 0;
        //秒杀还未开始
        if(nowDate.before(startDate)){
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime()))/1000; //转换为秒
        }else if(nowDate.after(endData)){
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            //秒杀进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds", remainSeconds);//秒杀活动开始倒计时
        model.addAttribute("secKillStatus", secKillStatus);//秒杀活动的状态（未开始 ，进行中，已结束）
        model.addAttribute("goods", goodsVo); //商品的详情对象
        return "goodsDetail";
    }
    */
    /*
    //url缓存优化后
    @RequestMapping(value = "toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model, User user, @PathVariable Long goodsId,
                            HttpServletRequest reques, HttpServletResponse reponse) {
        ValueOperations valueOperations = redisTemplate.opsForValue(); //创建一个string类型的redis对象
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);//缓存不同的ID的url缓存
        if (!StringUtils.isEmpty(html)) {
            //直接返回页面
            return html;
        }
        model.addAttribute("user", user);
        GoodsVo goodsVoByGoodsId = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVoByGoodsId.getStartDate();
        Date endDate = goodsVoByGoodsId.getEndDate();
        Date nowDate = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) (startDate.getTime() - nowDate.getTime())) / 1000;
        } else if (nowDate.after(endDate)) {
            //秒杀结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVoByGoodsId);
        //为空手动渲染，页面存入redis
        WebContext webContext = new WebContext(reques, reponse,
                reques.getServletContext(), reques.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }
    */
    @RequestMapping("toDetail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(User user, @PathVariable Long goodsId) {
        GoodsVo goodsVoByGoodsId = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVoByGoodsId.getStartDate();
        Date endDate = goodsVoByGoodsId.getEndDate();
        Date nowDate = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) (startDate.getTime() - nowDate.getTime())) / 1000;
        }else if (nowDate.after(endDate)){
            //秒杀结束
            seckillStatus = 2;
            remainSeconds = -1;
        }else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVoByGoodsId);
        detailVo.setSeckillStatus(seckillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }


}
