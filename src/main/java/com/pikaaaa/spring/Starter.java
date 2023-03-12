package com.pikaaaa.spring;

import com.pikaaaa.spring.anno.Autowired;
import com.pikaaaa.spring.anno.Component;
import com.pikaaaa.spring.anno.ComponentScan;
import com.pikaaaa.spring.entity.SingletonDemo;
import com.pikaaaa.spring.entity.PrototypeDemo;
import com.pikaaaa.spring.service.DemoService;

@ComponentScan("com.pikaaaa.spring")
@Component("starter")
public class Starter {

    @Autowired
    private static PrototypeDemo prototypeDemo;
    @Autowired
    private static SingletonDemo singletonDemo;
    @Autowired
    private static DemoService demoService;
    @Autowired
    private static DemoService demoService2;

    public static void main(String[] args) {

        SpringApplicationContext context = new SpringApplicationContext(Starter.class);
        System.out.println("原型模式");
        System.out.println(context.getBean("prototypeDemo"));
        System.out.println(prototypeDemo);
        System.out.println("=============");
        System.out.println("单例模式");
        System.out.println(context.getBean(SingletonDemo.class));
        System.out.println(singletonDemo);
        System.out.println("=============");
        System.out.println("Service依赖自动注入测试");
        System.out.println(demoService);
        System.out.println(demoService2);
    }

}
