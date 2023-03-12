package com.pikaaaa.spring;

import com.pikaaaa.spring.constant.ScopeEnum;

public class BeanStatus {

    private Class<?> bean;

    private ScopeEnum scope;

    public BeanStatus(Class<?> bean, ScopeEnum scope) {
        this.bean = bean;
        this.scope = scope;
    }

    public Class<?> getBean() {
        return bean;
    }

    public ScopeEnum getScope() {
        return scope;
    }
}
