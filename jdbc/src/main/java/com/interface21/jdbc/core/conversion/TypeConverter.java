package com.interface21.jdbc.core.conversion;

@FunctionalInterface
public interface TypeConverter {
    Object convertIfPossible(Object value, Class<?> targetType);
}
