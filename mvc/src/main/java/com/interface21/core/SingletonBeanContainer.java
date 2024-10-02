package com.interface21.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonBeanContainer implements BeanContainer {

    private static final SingletonBeanContainer instance = new SingletonBeanContainer();

    private final Map<Class<?>, Object> singletonObjects = new ConcurrentHashMap<>();

    private SingletonBeanContainer() {
    }

    public static SingletonBeanContainer getInstance() {
        return instance;
    }

    @Override
    public Object registerBean(Class<?> clazz) {
        if (singletonObjects.containsKey(clazz)) {
            return singletonObjects.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            Object classInstance = constructor.newInstance();
            constructor.setAccessible(false);
            singletonObjects.putIfAbsent(clazz, classInstance);
        } catch (IllegalArgumentException e) {
            throw new SingletonInstantiationException(clazz, "Arguments mismatch", e);
        } catch (IllegalAccessException e) {
            throw new SingletonInstantiationException(clazz, "Inaccessible constructor", e);
        } catch (InstantiationException e) {
            throw new SingletonInstantiationException(clazz, "Failed to instantiate class", e);
        } catch (InvocationTargetException e) {
            throw new SingletonInstantiationException(clazz, "Exception occurred on target class", e);
        } catch (NoSuchMethodException e) {
            throw new SingletonInstantiationException(clazz, "No constructor found (interface, array class, ..)", e);
        }
        return singletonObjects.get(clazz);
    }

    @Override
    public Object getBean(Class<?> clazz) {
        if (singletonObjects.containsKey(clazz)) {
            return singletonObjects.get(clazz);
        }
        throw new BeanNotFoundException(clazz);
    }

    @Override
    public List<Object> getAnnotatedBeans(Class<? extends Annotation> annotation) {
        return singletonObjects.values()
                .stream()
                .filter(obj -> obj.getClass().isAnnotationPresent(annotation))
                .toList();
    }
}
