package com.interface21.bean.container;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanContainer {

    private final Map<String, Object> container = new ConcurrentHashMap<>();

    private BeanContainer() {
    }

    public static BeanContainer getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public void registerBeans(List<Object> beans) {
        for (Object bean : beans) {
            String name = bean.getClass().getSimpleName();
            if (container.containsKey(name)) {
                throw new IllegalArgumentException(String.format("%s은 이미 등록된 빈입니다.", name));
            }
            container.put(name, bean);
        }
    }

    public <T> Object getBean(Class<T> clazz) {
        String name = clazz.getSimpleName();
        if (!container.containsKey(name)) {
            throw new IllegalArgumentException(String.format("%s는 존재하지 않는 빈입니다.", name));
        }
        return container.get(name);
    }

    public List<Object> getAnnotatedBeans(Class<? extends Annotation> annotation) {
        return container.values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .toList();
    }

    public <T> List<T> getSubTypeBeansOf(Class<T> clazz) {
        return container.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public void clear() {
        container.clear();
    }

    private static class SingletonHelper {
        private static final BeanContainer INSTANCE = new BeanContainer();
    }
}
