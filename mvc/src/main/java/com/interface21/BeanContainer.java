package com.interface21;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Configuration;
import com.interface21.context.stereotype.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
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
        setFiled(components);
    }

    private void setFiled(Set<Object> components) {
        for (Object bean : components) {
            setField(bean);
        }
    }

    private void setField(Object bean) {
        Arrays.stream(bean.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> setField(field, bean));
    }

    private void setField(Field field, Object bean) {
        Class<?> type = field.getType();
        beanRegistry.getBeans(type)
                .forEach(ConsumerWrapper.accept(b -> field.set(bean, b)));
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
