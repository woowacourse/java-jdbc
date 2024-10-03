package com.interface21.jdbc.core;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypeConverter {

    private static final Map<Class<?>, Class<?>> wrapperTypes = new HashMap<>();

    static {
        wrapperTypes.put(int.class, Integer.class);
        wrapperTypes.put(double.class, Double.class);
        wrapperTypes.put(boolean.class, Boolean.class);
        wrapperTypes.put(long.class, Long.class);
        wrapperTypes.put(float.class, Float.class);
        wrapperTypes.put(char.class, Character.class);
        wrapperTypes.put(byte.class, Byte.class);
        wrapperTypes.put(short.class, Short.class);
    }

    public static Class<?> convert(Class<?> primitiveType) {
        if (!wrapperTypes.containsKey(primitiveType)) {
            return primitiveType;
        }

        return wrapperTypes.get(primitiveType);
    }
}
