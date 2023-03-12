package com.pikaaaa.spring;

import com.pikaaaa.spring.anno.Autowired;
import com.pikaaaa.spring.anno.Component;
import com.pikaaaa.spring.anno.ComponentScan;
import com.pikaaaa.spring.anno.Service;
import com.pikaaaa.spring.exception.PikaSpringException;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {

    // 单例池
    private final ConcurrentHashMap<Object, Object> singletonPool = new ConcurrentHashMap<>();
    // 状态池
    private final ConcurrentHashMap<Object, BeanStatus> beanStatusPool = new ConcurrentHashMap<>();
    // class列表
    List<Class<?>> allClasses;
    public SpringApplicationContext(Class<?> clazz) {
        // 扫描
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = clazz.getDeclaredAnnotation(ComponentScan.class);
            String path = componentScan.value();
            URL resource = ClassLoader.getSystemResource(path.replace(".", "/"));
            try {
                File file = new File(resource.toURI());
                List<Component> componentList = new ArrayList<>();
                allClasses = getAllClasses(file);
                if (file.isDirectory()) {
                    for (Class<?> aClass: allClasses ) {
                        // 遍历全部component注解
                        initComponent(clazz, aClass);
                        initService(clazz, aClass);
                    }
                } else {
                    throw new PikaSpringException("文件目录异常");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new PikaSpringException("未找到扫描类");
        }
    }

    /**
     * 初始化Component注解
     * @param clazz 启动类
     * @param aClass 注解类
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private void initComponent(Class<?> clazz, Class<?> aClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (aClass.isAnnotationPresent(Component.class)) {
            // 当这个文件存在Component注解时
            Component component = aClass.getAnnotation(Component.class);
            // 判断模式
            Object value = component.value();
            if (value.equals("")) {
                value = aClass;
            }
            switch (component.scope()) {
                case SINGLETON:
                    // 将实例保存至单例池
                    singletonPool.put(value,
                            aClass.getDeclaredConstructor().newInstance(null));
                    // 状态池
                    beanStatusPool.put(value,
                            new BeanStatus(null, component.scope()));
                    break;
                case PROTOTYPE:
                    // 状态池
                    beanStatusPool.put(value,
                            new BeanStatus(aClass, component.scope()));
            }


            // 自动注入依赖
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                // 判断注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 类型检测
                    Class<?> fieldType = field.getType();
                    Object bean;
                    if (fieldType.isInterface()) {
                        // 判断为接口时 -> 查找实现类
                        bean = getBean(fieldType);
                    } else {
                        Component fieldComponent = fieldType.getAnnotation(Component.class);
                        bean = fieldComponent.value().equals("")?
                                getBean(fieldType):getBean(field.getName());
                    }
                    field.setAccessible(true);
                    field.set(clazz, bean);
                }
            }

        }
    }

    private void initService(Class<?> clazz, Class<?> aClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (aClass.isAnnotationPresent(Service.class)) {
            // 当这个文件存在Service注解时
            Service service = aClass.getAnnotation(Service.class);
            // 判断模式
            Object value = service.value();
            // 获取该类所有接口
            Class<?>[] interfaces = aClass.getInterfaces();
            if (interfaces.length>1) {
                // 如果数量大于1 错误
                throw new PikaSpringException("Bean接口不止一个");
            } else if (interfaces.length==0) {
                // 如果数量等于0则代表无接口返回原本对象
                if (value.equals("")) {
                    value = aClass;
                }
            } else {
                // 数量等于1 返回实现
                if (value.equals("")) {
                    value = interfaces[0];
                }
            }
            // 实例
            Object instance = aClass.getDeclaredConstructor().newInstance(null);;
            // 保存数据
            switch (service.scope()) {
                case SINGLETON:
                    // 将实例保存至单例池
                    singletonPool.put(value,
                            instance);
                    // 状态池
                    beanStatusPool.put(value,
                            new BeanStatus(null, service.scope()));
                    break;
                case PROTOTYPE:
                    // 状态池
                    beanStatusPool.put(value,
                            new BeanStatus(aClass, service.scope()));
            }


            // 自动注入依赖
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                // 判断注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 类型检测
                    Class<?> fieldType = field.getType();
                    Component fieldComponent = fieldType.getAnnotation(Component.class);
                    Object bean = fieldComponent.value().equals("")?
                            getBean(field.getType()):getBean(field.getName());
                    field.setAccessible(true);
                    field.set(clazz, bean);
                }
            }

        }
    }

    /**
     * 获取文件夹和子文件夹内的所有文件
     *
     * @param file
     * @return
     */
    public List<Class<?>> getAllClasses(File file) {
        List<Class<?>> list = new ArrayList<>();
        deepFileDir(file, list);
        return list;
    }

    private void deepFileDir(File file, List<Class<?>> outPutList) {
        if (file.isDirectory()) {
            // 当对象为文件夹时,继续向内遍历
            for (File f : file.listFiles()) {
                deepFileDir(f, outPutList);
            }
        } else {
            // 非文件夹
            String cPath = file.toString();
            cPath = cPath.substring(cPath.indexOf("com"), cPath.indexOf(".class"));
            cPath = cPath.replace("\\", ".");
            Class<?> aClass = null;
            try {
                aClass = Class.forName(cPath);
                outPutList.add(aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getBean(String beanName) {
        BeanStatus beanStatus = beanStatusPool.get(beanName);
        if (beanStatus == null) throw new PikaSpringException("未找到该bean:"+beanName);
        switch (beanStatus.getScope()) {
            case SINGLETON:
                return singletonPool.get(beanName);
            case PROTOTYPE:
                Constructor<?> declaredConstructor = null;
                try {
                    declaredConstructor = beanStatus.getBean().getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    return declaredConstructor.newInstance(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public Object getBean(Class<?> clazz) {
        BeanStatus beanStatus = beanStatusPool.get(clazz);
        if (beanStatus == null) throw new PikaSpringException("未找到该bean:"+clazz.toString());
        switch (beanStatus.getScope()) {
            case SINGLETON:
                return singletonPool.get(clazz);
            case PROTOTYPE:
                Constructor<?> declaredConstructor = null;
                try {
                    declaredConstructor = beanStatus.getBean().getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    return declaredConstructor.newInstance(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return null;
    }
}