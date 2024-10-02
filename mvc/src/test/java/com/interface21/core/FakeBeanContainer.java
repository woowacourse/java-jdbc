package com.interface21.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

public class FakeBeanContainer implements BeanContainer {

    private final Map<Class<?>, Object> beans;

    public FakeBeanContainer(Map<Class<?>, Object> beans) {
        this.beans = beans;
    }

    @Override
    public Object registerBean(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            Object bean = constructor.newInstance();
            return beans.put(clazz, bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getBean(Class<?> clazz) {
        return beans.get(clazz);
    }

    @Override
    public List<Object> getAnnotatedBeans(Class<? extends Annotation> annotation) {
        return beans.values()
                .stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .toList();
    }
}
