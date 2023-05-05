package com.pikaaaa.spring.entity;

import com.pikaaaa.spring.anno.Autowired;
import com.pikaaaa.spring.anno.Component;

import javax.annotation.PostConstruct;

@Component
public class SingletonDemo implements ISingletonDemo {

    //    @Autowired
    private PrototypeDemo prototypeDemo;

    public SingletonDemo() {

    }

    public SingletonDemo(PrototypeDemo prototypeDemo, String s) {
        this.prototypeDemo = prototypeDemo;
    }

    @Autowired
    public SingletonDemo(PrototypeDemo prototypeDemo) {
        this.prototypeDemo = prototypeDemo;
    }


    @Override
    public void doSomeThing() {
        System.out.println("prototypeDemo : " + prototypeDemo);
        System.out.println("执行了doSomeThing");
    }


    @PostConstruct
    public void a() {
        System.out.println("执行了PostConstruct");
    }

}
