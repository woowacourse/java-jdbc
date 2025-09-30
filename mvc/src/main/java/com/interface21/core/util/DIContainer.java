package com.interface21.core.util;

import com.interface21.context.stereotype.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class DIContainer {
    private final Map<Class<?>, Object> beanMap = new HashMap<>();
    private final Set<Class<?>> creatingBeans = new HashSet<>();

    public void scanAndRegister(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        componentClasses.stream()
                .filter(clazz -> !clazz.isInterface())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    try {
                        getBean(clazz);
                    } catch (RuntimeException e) {
                        throw e;
                    }
                });
    }

    public <T> T getBean(Class<T> type) {
        if (beanMap.containsKey(type)) {
            return type.cast(beanMap.get(type));
        }
        if (creatingBeans.contains(type)) {
            throw new IllegalStateException("Bean 순환오류 " + type.getName());
        }
        try {
            creatingBeans.add(type);
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            if (constructors.length != 1) {
                throw new IllegalStateException(("Bean은 하나이상의 public 생성자 필요." + type.getName()));
            }
            Constructor<?> constructor = constructors[0];
            constructor.setAccessible(true);
            Class<?>[] dependencyTypes = constructor.getParameterTypes();
            Object[] dependencies = new Object[dependencyTypes.length];

            for (int i = 0; i < dependencyTypes.length; i++) {
                dependencies[i] = getBean(dependencyTypes[i]);
            }
            T instance = type.cast(constructor.newInstance(dependencies));

            beanMap.put(type, instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(type.getName(), e);
        } finally {
            creatingBeans.remove(type);
        }
    }
}