package com.pikaaaa.spring.entity;

import com.pikaaaa.spring.BeanNameAware;
import com.pikaaaa.spring.InitializingBean;
import com.pikaaaa.spring.anno.Autowired;
import com.pikaaaa.spring.anno.Component;
import com.pikaaaa.spring.anno.Scope;
import com.pikaaaa.spring.constant.ScopeEnum;

@Component(value = "prototypeDemo")
@Scope(ScopeEnum.PROTOTYPE)
public class PrototypeDemo implements BeanNameAware, InitializingBean,IPrototypeDemo {

    public String beanName;

//    @Autowired
//    public SingletonDemo singletonDemo;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("执行了afterPropertiesSet");
    }

    @Override
    public void doSomeThing() {
        System.out.println("执行了doSomeThing");
    }
}
