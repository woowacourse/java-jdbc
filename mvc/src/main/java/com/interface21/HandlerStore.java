package com.interface21;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerStore {

    private static final Map<String, Object> MANAGERS = new ConcurrentHashMap<>();

    private HandlerStore() {}

    private static class Singleton {
        private static final HandlerStore INSTANCE = new HandlerStore();
    }

    public static HandlerStore getInstance() {
        return Singleton.INSTANCE;
    }

    public void registerHandler(List<Object> objects) {
        for (Object object : objects) {
            String clazzName = object.getClass().getName();
            validateDuplicateHandler(clazzName);
            MANAGERS.put(clazzName, object);
        }
    }

    private void validateDuplicateHandler(String clazzName) {
        if (MANAGERS.containsKey(clazzName)) {
            throw new IllegalArgumentException("이미 등록되어 있는 클래스입니다");
        }
    }

    public <T> List<T> getHandler(Class<T> clazz) {
        return MANAGERS.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public List<Object> getHandlerWithAnnotation(Class<? extends Annotation> annotation) {
        return MANAGERS.values().stream()
                .filter(clazz -> clazz.getClass().isAnnotationPresent(annotation))
                .toList();
    }

    public void clear() {
        MANAGERS.clear();
    }
}
