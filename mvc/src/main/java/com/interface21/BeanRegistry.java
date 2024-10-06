package com.interface21;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BeanRegistry {

    private static final Map<String, Object> MANAGERS = new ConcurrentHashMap<>();

    private BeanRegistry() {}

    private static class Singleton {
        private static final BeanRegistry INSTANCE = new BeanRegistry();
    }

    public static BeanRegistry getInstance() {
        return Singleton.INSTANCE;
    }

    public void registerBeans(Set<Object> objects) {
        for (Object object : objects) {
            String clazzName = object.getClass().getName();
            validateDuplicateBean(clazzName);
            MANAGERS.put(clazzName, object);
        }
    }

    private void validateDuplicateBean(String clazzName) {
        if (MANAGERS.containsKey(clazzName)) {
            throw new IllegalArgumentException("이미 등록되어 있는 클래스입니다");
        }
    }

    public <T> List<T> getBeans(Class<T> clazz) {
        return MANAGERS.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public List<Object> getBeansWithAnnotation(Class<? extends Annotation> annotation) {
        return MANAGERS.values().stream()
                .filter(clazz -> clazz.getClass().isAnnotationPresent(annotation))
                .toList();
    }

    public void clear() {
        MANAGERS.clear();
    }
}
