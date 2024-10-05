package com.interface21;

import com.interface21.core.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.reflections.Reflections;

public class HandlerScanner {

    private HandlerScanner() {}

    public static List<Object> scanTypesAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        return reflections.getTypesAnnotatedWith(annotation).stream()
                .map(HandlerScanner::createObject)
                .toList();
    }

    public static List<Object> scanSubTypeOf(Class<?> clazz, Class<?> type) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        return reflections.getSubTypesOf(type).stream()
                .map(HandlerScanner::createObject)
                .toList();
    }

    private static Object createObject(Class<?> clazz) {
        try {
            return ReflectionUtils.accessibleConstructor(clazz).newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("기본 생성자가 존재하지 않습니다");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
