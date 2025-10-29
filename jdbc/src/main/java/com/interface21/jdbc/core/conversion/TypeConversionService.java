package com.interface21.jdbc.core.conversion;

import com.interface21.jdbc.core.util.PrimitiveUtils;
import java.util.ArrayList;
import java.util.List;

public class TypeConversionService {

    private final TypeConverter DEFAULT_CONVERTER = DefaultTypeConverters::applyDefaultConversion;
    private final List<TypeConverter> registeredConverters;

    public TypeConversionService(List<TypeConverter> registeredConverters) {
        this.registeredConverters = registeredConverters;
    }

    public TypeConversionService() {
        this.registeredConverters = new ArrayList<>();
    }

    public Object convert(final Object sourceValue, final Class<?> targetType) {
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

        final var convertedByDefault = DEFAULT_CONVERTER.convertIfPossible(sourceValue, targetType);
        if (hasConversionOccurred(convertedByDefault, sourceValue) && isCompatibleType(targetType, convertedByDefault)) {
            return convertedByDefault;
        }
        return sourceValue;
    }

    private Object resolveNullValueBy(final Class<?> targetType) {
        if (targetType.isPrimitive()) {
            return PrimitiveUtils.getDefaultValue(targetType);
        }
        return null;
    }

    private Object convertWithRegisteredConverters(final Object sourceValue, final Class<?> targetType) {
        for (final TypeConverter converter : registeredConverters) {
            final var converted = converter.convertIfPossible(sourceValue, targetType);
            if (hasConversionOccurred(converted, sourceValue) && isCompatibleType(targetType, converted)) {
                return converted;
            }
        }
        return sourceValue;
    }

    /**
     * 변환된 객체를 원본과 비교하여 실제로 변환이 발생했는지 확인합니다.
     * 변환된 객체가 원본과 다른 경우 true를 반환합니다.
     */
    private boolean hasConversionOccurred(final Object converted, final Object original) {
        return converted != original;
    }

    /**
     * 소스 값이 이미 대상 타입과 일치하는지 확인합니다.
     */
    private boolean isAlreadyTargetType(Object sourceValue, Class<?> targetType) {
        return isCompatibleType(targetType, sourceValue);
    }

    /**
     * 대상 타입과 값이 호환되는지 확인합니다.
     * 원시 타입과 래퍼 타입(int ↔ Integer 등)을 동등하게 처리하여 호환성을 보장합니다.
     */
    private boolean isCompatibleType(Class<?> targetType, Object value) {
        if (value == null) {
            return false;
        }
        Class<?> effectiveTargetType = targetType.isPrimitive() ? PrimitiveUtils.getWrapperType(targetType) : targetType;
        return effectiveTargetType.isInstance(value);
    }
}
