package com.pikaaaa.spring;

import com.pikaaaa.spring.anno.*;
import com.pikaaaa.spring.constant.ScopeEnum;
import com.pikaaaa.spring.exception.PikaSpringException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {

    // 单例池 --- 1 级 缓存
    private final ConcurrentHashMap<Object, Object> singletonObjects = new ConcurrentHashMap<>();
    // 未初始化 单例池 --- 2 级 缓存
    private final ConcurrentHashMap<Object, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // --- 3 级 缓存
    private final ConcurrentHashMap<Object, Object> singletonFactories = new ConcurrentHashMap<>();
    // 状态池
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // beanPostProcessor池
    private final ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
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
                    for (Class<?> aClass : allClasses) {
                        // 遍历全部component注解
                        initComponent(clazz, aClass);
//                        initService(clazz, aClass);
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
     *
     * @param clazz  启动类
     * @param aClass 注解类
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private void initComponent(Class<?> clazz, Class<?> aClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (aClass.isAnnotationPresent(Component.class)) {
            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                BeanPostProcessor instance = (BeanPostProcessor) aClass.newInstance();
                beanPostProcessorList.add(instance);
            }
            // 当这个文件存在Component注解时
            Component component = aClass.getAnnotation(Component.class);
            // 判断模式
            String value = component.value();
            if (value.equals("")) {
                value = decapitalize(aClass.getSimpleName());
            }
            ScopeEnum scope = ScopeEnum.SINGLETON;
            if (aClass.isAnnotationPresent(Scope.class)) {
                // 如果存在Scope注解
                Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                scope = scopeAnnotation.value();
            }
            // 放入map池
            BeanDefinition beanDefinition = new BeanDefinition(aClass, scope);
            beanDefinitionMap.put(value, beanDefinition);


        }
    }

//    private void initService(Class<?> clazz, Class<?> aClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        if (aClass.isAnnotationPresent(Service.class)) {
//            // 当这个文件存在Service注解时
//            Service service = aClass.getAnnotation(Service.class);
//            // 判断模式
//            Object value = service.value();
//            // 获取该类所有接口
//            Class<?>[] interfaces = aClass.getInterfaces();
//            if (interfaces.length>1) {
//                // 如果数量大于1 错误
//                throw new PikaSpringException("Bean接口不止一个");
//            } else if (interfaces.length==0) {
//                // 如果数量等于0则代表无接口返回原本对象
//                if (value.equals("")) {
//                    value = aClass;
//                }
//            } else {
//                // 数量等于1 返回实现
//                if (value.equals("")) {
//                    value = interfaces[0];
//                }
//            }
//            // 实例
//            Object instance = aClass.getDeclaredConstructor().newInstance(null);;
//            // 保存数据
//            switch (service.scope()) {
//                case SINGLETON:
//                    // 将实例保存至单例池
//                    singletonPool.put(value,
//                            instance);
//                    // 状态池
//                    beanInitializationConcurrentHashMap.put(value,
//                            new BeanDefinition(null, service.scope()));
//                    break;
//                case PROTOTYPE:
//                    // 状态池
//                    beanInitializationConcurrentHashMap.put(value,
//                            new BeanDefinition(aClass, service.scope()));
//            }
//
//
//            // 自动注入依赖
//            Field[] fields = aClass.getDeclaredFields();
//            for (Field field : fields) {
//                // 判断注解
//                if (field.isAnnotationPresent(Autowired.class)) {
//                    // 类型检测
//                    Class<?> fieldType = field.getType();
//                    Component fieldComponent = fieldType.getAnnotation(Component.class);
//                    Object bean = fieldComponent.value().equals("")?
//                            getBean(field.getType()):getBean(field.getName());
//                    field.setAccessible(true);
//                    field.set(clazz, bean);
//                }
//            }
//
//        }
//    }

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


    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        try {
            // 获取所有的构造器
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> constructor = null; // 最后实例化的构造器
            Object[] params = null;
            if (constructors.length > 1) {
                // 如果构造器大于1个 , 选择无参构造
                for (Constructor<?> c : constructors) {
                    Type[] parameterTypes = c.getGenericParameterTypes();
                    if (parameterTypes.length==0) {
                        constructor = c;
                    }
                    if (c.isAnnotationPresent(Autowired.class)) {
                        // 如果有Autowired注解 则使用该构造方法
                        constructor = c;
                        params = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            String typeName = parameterTypes[i].getTypeName();
                            params[i] = getBean(decapitalize(typeName.substring(typeName.lastIndexOf('.') + 1)));
                        }
                        break;
                    }
                }
                if (constructor == null) throw new NullPointerException("未找到正确构造器");
            } else {
                constructor = constructors[0];
            }
            Object instance = constructor.newInstance(params);
            // 属性注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }
            // 初始化前 -> PostConstruct
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(PostConstruct.class)) {
                    m.invoke(instance, null);
                }
            }
            // Aware
            if (instance instanceof BeanNameAware) {
                // 如果实现了BeanNameAware接口
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessorBeforeInitialization(beanName, instance);
            }
            // 初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }
            // AOP
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessorAfterInitialization(beanName, instance);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) throw new PikaSpringException("未找到该bean:" + beanName);
        switch (beanDefinition.getScope()) {
            case SINGLETON:
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            case PROTOTYPE:
                return createBean(beanName, beanDefinition);
        }
        return null;
    }

//    public Object getBean(Class<?> clazz) {
//        BeanDefinition beanDefinition = beanInitializationConcurrentHashMap.get(clazz);
//        if (beanDefinition == null) throw new PikaSpringException("未找到该bean:"+clazz.toString());
//        switch (beanDefinition.getScope()) {
//            case SINGLETON:
//                return singletonPool.get(clazz);
//            case PROTOTYPE:
//                Constructor<?> declaredConstructor = null;
//                try {
//                    declaredConstructor = beanDefinition.getType().getDeclaredConstructor();
//                    declaredConstructor.setAccessible(true);
//                    return declaredConstructor.newInstance(null);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//        }
//        return null;
//    }

    public String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0))
                && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}