package com.interface21.webmvc.servlet.mvc;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class SingletonManager {

    private static final Map<Class<?>, Object> MANAGER = new HashMap<>();

    public static Object getOrSaveObject(Class<?> clazz) {
        if (MANAGER.containsKey(clazz)) {
            return MANAGER.get(clazz);
        }
        return createObject(clazz);
    }

    private static Object createObject(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object object = constructor.newInstance();
            MANAGER.put(clazz, object);
            return object;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("기본 생성자가 존재하지 않습니다");
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
