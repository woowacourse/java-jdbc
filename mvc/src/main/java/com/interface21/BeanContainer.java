package com.interface21;

import com.interface21.context.stereotype.Component;
import java.lang.annotation.Annotation;
import java.util.List;

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

    public void initialize(Class<?> clazz) {
        registerHandlerManagement(clazz);
        if (!clazz.getPackageName().equals(this.getClass().getPackageName())) {
            registerHandlerManagement(this.getClass());
        }
    }

    public void registerHandlerManagement(Class<?> clazz) {
        List<Object> controllers = BeanScanner.scanTypesAnnotatedWith(clazz, Component.class);
        beanRegistry.registerHandler(controllers);
    }

    public List<Object> getHandlerWithAnnotation(Class<? extends Annotation> annotation) {
        return beanRegistry.getHandlerWithAnnotation(annotation);
    }

    public <T> List<T> getHandlers(Class<T> clazz) {
        return beanRegistry.getHandler(clazz);
    }

    public void clear() {
        beanRegistry.clear();
    }
}
