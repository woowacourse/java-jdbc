package com.interface21;

import com.interface21.context.stereotype.Controller;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerMapping;
import java.lang.annotation.Annotation;
import java.util.List;

public class HandlerContainer {

    private final HandlerStore handlerStore;

    private HandlerContainer() {
        handlerStore = HandlerStore.getInstance();
    }

    private static class Singleton {
        private static final HandlerContainer INSTANCE = new HandlerContainer();
    }

    public static HandlerContainer getInstance() {
        return Singleton.INSTANCE;
    }

    public void initialize(Class<?> clazz) {
        registerHandlerManagement(clazz);
        if (!clazz.getPackageName().equals(this.getClass().getPackageName())) {
            registerHandlerManagement(this.getClass());
        }
    }

    public void registerHandlerManagement(Class<?> clazz) {
        List<Object> mappings = HandlerScanner.scanSubTypeOf(clazz, HandlerMapping.class);
        handlerStore.registerHandler(mappings);
        List<Object> adapters = HandlerScanner.scanSubTypeOf(clazz, HandlerAdapter.class);
        handlerStore.registerHandler(adapters);
        List<Object> controllers = HandlerScanner.scanTypesAnnotatedWith(clazz, Controller.class);
        handlerStore.registerHandler(controllers);
    }

    public List<Object> getHandlerWithAnnotation(Class<? extends Annotation> annotation) {
        return handlerStore.getHandlerWithAnnotation(annotation);
    }

    public <T> List<T> getHandlers(Class<T> clazz) {
        return handlerStore.getHandler(clazz);
    }

    public void clear() {
        handlerStore.clear();
    }
}
