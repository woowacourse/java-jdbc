package com.interface21;

import com.interface21.context.stereotype.Bean;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanCreator {

    public static Set<Object> makeComponents(Set<Class<?>> componentClasses) {
        return componentClasses.stream()
                .map(FunctionWrapper.apply(Class::getDeclaredConstructor))
                .peek(constructor -> constructor.setAccessible(true))
                .map(FunctionWrapper.apply(Constructor::newInstance))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<Object> makeConfiguration(Set<Class<?>> configurationClasses) {
        return configurationClasses.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(Bean.class))
                        .peek(method -> method.setAccessible(true))
                        .map(method -> getObject(clazz, method))
                ).collect(Collectors.toUnmodifiableSet());
    }

    private static Object getObject(Class<?> clazz, Method method) {
        try {
            return method.invoke(clazz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
