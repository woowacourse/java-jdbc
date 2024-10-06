package com.interface21;

import com.interface21.context.stereotype.Bean;
import com.interface21.context.stereotype.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanCreator {

    private static final BeanRegistry beanRegistry = BeanRegistry.getInstance();

    public static Set<Object> makeBeans(Set<Class<?>> classes) {
        Set<Object> constructors = classes.stream()
                .map(FunctionWrapper.apply(Class::getDeclaredConstructor))
                .peek(constructor -> constructor.setAccessible(true))
                .map(FunctionWrapper.apply(Constructor::newInstance))
                .collect(Collectors.toUnmodifiableSet());
        beanRegistry.registerHandler(constructors.stream().toList());
        setFiled(constructors);
        return constructors;
    }

    private static void setFiled(Set<Object> constructors) {
        for (Object bean : constructors) {
            setField(bean);
        }
    }

    private static void setField(Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Class<?> type = field.getType();

            beanRegistry.getHandler(type)
                    .forEach(ConsumerWrapper.accept(b -> field.set(bean, b)));
        }
    }

    public static Set<Object> makeConfiguration(Set<Class<?>> typesAnnotatedWith) {
        Set<Object> objects = new HashSet<>();
        for (Class<?> c : typesAnnotatedWith) {
            Method[] methods = c.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(Bean.class)) {
                    continue;
                }
                method.setAccessible(true);
                try {
                    Object invoke = method.invoke(c);
                    objects.add(invoke);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        beanRegistry.registerHandler(objects.stream().toList());
        return objects;
    }
}
