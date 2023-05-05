package com.pikaaaa.spring;

import com.pikaaaa.spring.anno.Autowired;
import com.pikaaaa.spring.anno.Component;
import com.pikaaaa.spring.anno.ComponentScan;
import com.pikaaaa.spring.entity.IPrototypeDemo;
import com.pikaaaa.spring.entity.ISingletonDemo;
import com.pikaaaa.spring.entity.SingletonDemo;
import com.pikaaaa.spring.entity.PrototypeDemo;
import com.pikaaaa.spring.service.DemoService;

@ComponentScan("com.pikaaaa.spring")
@Component
public class Starter {


    public static void main(String[] args) {

        SpringApplicationContext context = new SpringApplicationContext(Starter.class);
        ISingletonDemo singletonDemo = (ISingletonDemo) context.getBean("singletonDemo");
        singletonDemo.doSomeThing();
    }

}
