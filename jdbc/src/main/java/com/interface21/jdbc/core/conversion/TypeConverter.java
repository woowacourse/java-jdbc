package com.interface21.jdbc.core.conversion;

public interface TypeConverter {
    Object convertIfPossible(Object value, Class<?> targetType);
}
