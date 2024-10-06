package com.interface21;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

public class BeanScanner {

    private BeanScanner() {}

    public static Set<Class<?>> scanTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(ClasspathHelper.forJavaClassPath());
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
