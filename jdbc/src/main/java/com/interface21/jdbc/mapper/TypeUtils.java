package com.interface21.jdbc.mapper;

import java.util.Map;

public class TypeUtils {

    private TypeUtils() {}

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = Map.of(
            int.class, Integer.class,
            long.class, Long.class,
            double.class, Double.class,
            float.class, Float.class,
            boolean.class, Boolean.class,
            char.class, Character.class
    );

    public static boolean isCompatible(Class<?> targetType, Class<?> valueType) {
        if (targetType.equals(valueType)) {
            return true;
        }

        if (targetType.isPrimitive()) {
            return PRIMITIVE_WRAPPER_MAP.get(targetType).equals(valueType);
        }

        if (valueType.isPrimitive()) {
            return PRIMITIVE_WRAPPER_MAP.get(valueType).equals(targetType);
        }

        return false;
    }
}
