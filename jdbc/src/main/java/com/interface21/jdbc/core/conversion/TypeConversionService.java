package com.interface21.jdbc.core.conversion;

import com.interface21.jdbc.core.util.PrimitiveUtils;

public final class TypeConversionService {

    private static final TypeConverter DEFAULT_CONVERTER = DefaultTypeConverters::tryConvert;

    public static Object convert(final Object sourceValue, final Class<?> targetType) {
        if (sourceValue == null) {
            return resolveNullValueBy(targetType);
        }
        if (isAlreadyTargetType(sourceValue, targetType)) {
            return sourceValue;
        }

        final var convertedByRegistered = convertWithRegisteredConverters(sourceValue, targetType);
        if (hasConversionOccurred(convertedByRegistered, sourceValue)) {
            return convertedByRegistered;
        }

        final var convertedByDefault = DEFAULT_CONVERTER.convert(sourceValue, targetType);
        if (hasConversionOccurred(convertedByDefault, sourceValue) && targetType.isInstance(convertedByDefault)) {
            return convertedByDefault;
        }
        return sourceValue;
    }

    private static Object resolveNullValueBy(final Class<?> targetType) {
        if (targetType.isPrimitive()) {
            return PrimitiveUtils.getDefaultValue(targetType);
        }
        return null;
    }

    private static Object convertWithRegisteredConverters(final Object sourceValue, final Class<?> targetType) {
        for (final TypeConverter converter : TypeConverterRegistry.getConverters()) {
            final var converted = converter.convert(sourceValue, targetType);
            if (hasConversionOccurred(converted, sourceValue) && targetType.isInstance(converted)) {
                return converted;
            }
        }
        return sourceValue;
    }

    /**
     * 변환된 객체를 원본과 비교하여 실제로 변환이 발생했는지 확인합니다.
     * 변환된 객체가 원본과 다른 경우 true를 반환합니다.
     */
    private static boolean hasConversionOccurred(final Object converted, final Object original) {
        return converted != original;
    }

    /**
     * 소스 값이 이미 대상 타입과 일치하는지 확인합니다.
     */
    private static boolean isAlreadyTargetType(Object sourceValue, Class<?> targetType) {
        return targetType.isInstance(sourceValue);
    }
}
