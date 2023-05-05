package com.pikaaaa.spring;

import com.pikaaaa.spring.constant.ScopeEnum;

public class BeanDefinition {

    private Class type;

    private ScopeEnum scope;

    public BeanDefinition(Class type, ScopeEnum scope) {
        this.type = type;
        this.scope = scope;
    }

    public BeanDefinition() {
    }

    public Class getType() {
        return type;
    }

    public ScopeEnum getScope() {
        return scope;
    }
}
