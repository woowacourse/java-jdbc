package com.interface21;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Configuration;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

public class BeanScanner {

    private BeanScanner() {}

    public static List<Object> scanTypesAnnotatedWith() {
        Reflections reflections = new Reflections(ClasspathHelper.forJavaClassPath());
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Configuration.class);
        Set<Object> objects = BeanCreator.makeConfiguration(typesAnnotatedWith);
        objects.addAll(BeanCreator.makeBeans(classes));
        return objects.stream()
                .toList();
    }

    public static List<Object> scanTypesAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
        return BeanCreator.makeBeans(classes).stream()
                .toList();
    }
}
