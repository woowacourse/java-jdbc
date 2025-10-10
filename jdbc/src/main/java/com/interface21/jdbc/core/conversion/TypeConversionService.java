package com.interface21.jdbc.core.conversion;

public final class TypeConversionService {

    private static final TypeConverter DEFAULT_CONVERTER = DefaultTypeConverters::tryConvert;

    public static Object convert(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }

        for (TypeConverter converter : TypeConverterRegistry.getConverters()) {
            Object result = converter.convert(value, targetType);
            if (result != value && targetType.isInstance(result)) {
                return result;
            }
        }

        Object defaultResult = DEFAULT_CONVERTER.convert(value, targetType);
        if (defaultResult != value && targetType.isInstance(defaultResult)) {
            return defaultResult;
        }
        return value;
    }
}
