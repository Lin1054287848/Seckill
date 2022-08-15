package com.xxxx.seckill.vo;

import com.xxxx.seckill.utils.ValidatorUtil;
import com.xxxx.seckill.validator.IsMoblie;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//手机号码校验规则
public class IsMobileValidator implements ConstraintValidator<IsMoblie, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMoblie constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (required) { //判断是否为必填信息
            return ValidatorUtil.isMobile(value);
        }else { //非必填
            if (StringUtils.isEmpty(value)) {
                return true;
            }else {
                return ValidatorUtil.isMobile(value); //校验工具 校验
            }
        }
    }
}
