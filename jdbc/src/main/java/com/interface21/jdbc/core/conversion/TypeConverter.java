package com.interface21.jdbc.core.conversion;

public interface TypeConverter {
    Object convert(Object value, Class<?> targetType);
}
