package com.xxxx.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * //限流采用的是计数器算法
 * //更好的限流算法：滑动窗口（zset）、漏铜算法、令牌桶算法
 */
@Retention(RetentionPolicy.RUNTIME) //运行时的
@Target(ElementType.METHOD) //设置在方法上就能够执行
public @interface AccessLimit {

    int second();

    int maxCount();

    boolean needLogin() default true;
}
