package com.interface21.webmvc.servlet.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodParameter {

    private final Method method;
    private final Parameter parameter;

    public MethodParameter(Method method, int index) {
        this.method = method;
        this.parameter = method.getParameters()[index];
    }

    public boolean isMethodAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }

    public boolean isParameterAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    public boolean isType(Class<?> clazz) {
        return getType().equals(clazz);
    }

    public <T extends Annotation> T getMethodAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public <T extends Annotation> T getParameterAnnotation(Class<T> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    public Class<?> getType() {
        return parameter.getType();
    }
}
