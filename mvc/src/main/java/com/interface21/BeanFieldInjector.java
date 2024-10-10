package com.interface21;

import com.interface21.context.stereotype.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

public class BeanFieldInjector {

    private static final BeanRegistry BEAN_REGISTRY = BeanRegistry.getInstance();

    public static void setFiled(Set<Object> beans) {
        for (Object bean : beans) {
            setField(bean);
        }
    }

    private static void setField(Object bean) {
        Arrays.stream(bean.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> setField(field, bean));
    }

    private static void setField(Field field, Object bean) {
        Class<?> type = field.getType();
        BEAN_REGISTRY.getBeans(type)
                .forEach(ConsumerWrapper.accept(b -> field.set(bean, b)));
    }
}
