package com.xxxx.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;



/**
 * MD5工具类
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 * @since 1.0.0
 */
@Component
public class MD5Util {

    public static String md5(String src){ //md5加密
        return DigestUtils.md5Hex(src);
    }

    private static final String salt="1a2b3c4d"; //加密的salt


    public static String inputPassToFromPass(String inputPass){//获取从客户端 -> 后台的 密码
        String str = "" +salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4); //提高安全性 随机取盐的4个值拼接密码“12+inputPass+c3”
        return md5(str);//密码第一次加密 客户端 -> 后端前 加密 //避免密码在网络中明文传输
    }

    public static String formPassToDBPass(String formPass,String salt){//获取从后台-> 数据库 的密码
        String str = "" +salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4); //第二次的salt是随机盐
        return md5(str);//密码第二次加密 后端 -> 数据库
    }

    public static String inputPassToDBPass(String inputPass,String salt){  //获取从客户端 -> 数据库的 最终的密码
        String fromPass = inputPassToFromPass(inputPass);
        String dbPass = formPassToDBPass(fromPass, salt);
        return dbPass; //得到最终的数据库的盐
    }


    public static void main(String[] args) { //MD5两次加密测试
        // d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToFromPass("123456"));
        System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }

}