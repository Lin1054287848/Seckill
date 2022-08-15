package com.xxxx.seckill.utils;


import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//参数校验
//手机号码校验
public class ValidatorUtil {
    private static final Pattern mobile_patten = Pattern.compile("[1]([3-9])[0-9]{9}$"); //正则表达式：第一位为1、第二位为3-9、后面9位在0-9之间（[0-9]{9}$）

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) { //判断是否为空
            return false;
        }
        Matcher matcher = mobile_patten.matcher(mobile);
        return matcher.matches();//手机号码校验 true或false
    }
}