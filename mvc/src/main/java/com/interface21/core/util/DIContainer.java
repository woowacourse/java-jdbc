package com.interface21.core.util;

import com.interface21.context.stereotype.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class DIContainer {
    private final Map<Class<?>, Object> beanMap = new HashMap<>();

    public void scanAndRegister(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        componentClasses.stream()
                .filter(clazz -> !clazz.isInterface())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    try {
                        getBean(clazz);
                    } catch (Exception e) {
                        throw new RuntimeException(clazz.getName(), e);
                    }
                });
    }

    public <T> T getBean(Class<T> type) {
        if (beanMap.containsKey(type)) {
            return type.cast(beanMap.get(type));
        }

        try {
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            Class<?>[] dependencyTypes = constructor.getParameterTypes();
            Object[] dependencies = new Object[dependencyTypes.length];

            for (int i = 0; i < dependencyTypes.length; i++) {
                dependencies[i] = getBean(dependencyTypes[i]);
            }
            T instance = type.cast(constructor.newInstance(dependencies));

            beanMap.put(type, instance);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
