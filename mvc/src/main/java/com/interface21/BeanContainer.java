package com.interface21;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Configuration;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public class BeanContainer {

    private final BeanRegistry beanRegistry;

    private BeanContainer() {
        beanRegistry = BeanRegistry.getInstance();
    }

    private static class Singleton {
        private static final BeanContainer INSTANCE = new BeanContainer();
    }

    public static BeanContainer getInstance() {
        return Singleton.INSTANCE;
    }

    public void initialize() {
        registerBeans();
    }

    private void registerBeans() {
        Set<Class<?>> configurationClasses = BeanScanner.scanTypesAnnotatedWith(Configuration.class);
        Set<Object> configuration = BeanCreator.makeConfiguration(configurationClasses);
        beanRegistry.registerBeans(configuration);
        Set<Class<?>> componentClasses = BeanScanner.scanTypesAnnotatedWith(Component.class);
        Set<Object> components = BeanCreator.makeComponents(componentClasses);
        beanRegistry.registerBeans(components);
        BeanFieldInjector.setFiled(components);
    }

    public List<Object> getBeansWithAnnotation(Class<? extends Annotation> annotation) {
        return beanRegistry.getBeansWithAnnotation(annotation);
    }

    public <T> List<T> getBeans(Class<T> clazz) {
        return beanRegistry.getBeans(clazz);
    }

    public void clear() {
        beanRegistry.clear();
    }
}
