package com.interface21.jdbc.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum WrapperToPrimitiveConverter {
    BOOLEAN(Boolean.class, boolean.class, (Object o) -> ((Boolean) o).booleanValue()),
    BYTE(Byte.class, byte.class, (Object o) -> ((Byte) o).byteValue()),
    CHARACTER(Character.class, char.class, (Object o) -> ((Character) o).charValue()),
    DOUBLE(Double.class, double.class, (Object o) -> ((Double) o).doubleValue()),
    FLOAT(Float.class, float.class, (Object o) -> ((Float) o).floatValue()),
    INTEGER(Integer.class, int.class, (Object o) -> ((Integer) o).intValue()),
    LONG(Long.class, long.class, (Object o) -> ((Long) o).longValue()),
    SHORT(Short.class, short.class, (Object o) -> ((Short) o).shortValue());

    private static final Map<Class<?>, Class<?>> CLASSIFY =
            Arrays.stream(values())
                    .collect(Collectors.toMap(v -> v.wrapperClass, v -> v.primitiveClass));
    private final Class<?> wrapperClass;
    private final Class<?> primitiveClass;
    private final Function<Object, ?> converter;

    WrapperToPrimitiveConverter(final Class<?> wrapperClass, final Class<?> primitiveClass, final Function<Object, ?> converter) {
        this.wrapperClass = wrapperClass;
        this.primitiveClass = primitiveClass;
        this.converter = converter;
    }

    public static <T> T getPrimitiveValue(final Object value) {
        for (final WrapperToPrimitiveConverter type : values()) {
            if (type.wrapperClass.equals(value.getClass())) {
                return (T) type.converter.apply(value);
            }
        }
        return (T) value;
    }

    public static Class<?> getPrimitiveClass(final Class<?> clazz) {
        return CLASSIFY.getOrDefault(clazz, clazz);
    }
}

