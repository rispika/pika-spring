package com.pikaaaa.spring.anno;

import com.pikaaaa.spring.constant.ScopeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {

    ScopeEnum value() default ScopeEnum.SINGLETON; ;
}
