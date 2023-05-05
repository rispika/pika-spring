package com.pikaaaa.spring.entity;

import com.pikaaaa.spring.BeanPostProcessor;
import com.pikaaaa.spring.anno.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitialization(String beanName, Object bean) {
        if (beanName.equals("singletonDemo")) {
            System.out.println("执行postProcessorBeforeInitialization");
        }
        return bean;
    }

    @Override
    public Object postProcessorAfterInitialization(String beanName, Object bean) {
        if (beanName.equals("singletonDemo")) {
            System.out.println("执行postProcessorAfterInitialization");
            Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    Object invoke = method.invoke(bean, args);
                    System.out.println("切面逻辑完成");
                    return invoke;
                }
            });
            return proxyInstance;
        }

        return bean;
    }
}
