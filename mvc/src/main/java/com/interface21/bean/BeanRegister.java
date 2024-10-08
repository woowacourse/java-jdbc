package com.interface21.bean;

import com.interface21.bean.container.BeanContainer;
import com.interface21.bean.scanner.BeanCreationException;
import com.interface21.bean.scanner.SubTypeScanner;
import com.interface21.bean.scanner.ComponentScanner;
import com.interface21.core.util.ReflectionUtils;
import com.interface21.webmvc.servlet.mvc.ArgumentResolver;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import com.interface21.webmvc.servlet.mvc.ReturnValueHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BeanRegister {

    private BeanRegister() {
    }

    public static void run(Class<?> app) {
        BeanContainer beanContainer = BeanContainer.getInstance();
        registerInternalBean(beanContainer);
        registerExternalBean(beanContainer, app);
    }

    private static void registerInternalBean(BeanContainer beanContainer) {
        registerSubTypeBean(beanContainer, ReturnValueHandler.class);
        registerSubTypeBean(beanContainer, ArgumentResolver.class);
        registerSubTypeBean(beanContainer, HandlerAdapter.class);
        registerSubTypeBean(beanContainer, HandlerMapping.class);
    }

    private static void registerSubTypeBean(BeanContainer beanContainer, Class<?> clazz) {
        List<Object> beans = SubTypeScanner.subTypeScan(clazz, clazz.getPackageName()).stream()
                .map(BeanRegister::createBean)
                .toList();
        beanContainer.registerBeans(beans);
    }

    private static void registerExternalBean(BeanContainer beanContainer, Class<?> clazz) {
        String packageName = clazz.getPackageName();
        List<Object> beans = ComponentScanner.componentScan(packageName).stream()
                .map(BeanRegister::createBean)
                .toList();
        beanContainer.registerBeans(beans);
    }

    public static Object createBean(Class<?> clazz) {
        try {
            return ReflectionUtils.accessibleConstructor(clazz).newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new BeanCreationException("빈을 생성할 수 없습니다.", e);
        }
    }
}
