package com.xxxx.seckill.config;

import com.xxxx.seckill.pojo.User;
//
public class UserContext { //一个线程用户的值

    private static ThreadLocal<User> userHolder = new ThreadLocal<>(); //每个线程绑定自己的值 线程安全

    public static void setUser(User user) { //设置用户
        userHolder.set(user);
    }

    public static User getUser() { //获取用户
        return userHolder.get();
    }

}