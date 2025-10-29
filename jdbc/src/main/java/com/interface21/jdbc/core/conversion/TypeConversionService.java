package com.interface21.jdbc.core.conversion;

import com.interface21.jdbc.core.util.PrimitiveUtils;
import java.util.ArrayList;
import java.util.List;

public class TypeConversionService {

    private final List<TypeConverter> registeredConverters;

    public TypeConversionService(List<TypeConverter> registeredConverters) {
        this.registeredConverters = registeredConverters;
    }

    public TypeConversionService() {
        this.registeredConverters = new ArrayList<>();
    }

    /**
     * 주어진 sourceValue를 targetType으로 변환합니다.
     */
    public Object convert(final Object sourceValue, final Class<?> targetType) {
        if (sourceValue == null) {
            return resolveNullValueBy(targetType);
        }
        // 이미 대상 타입과 동일한 경우 변환 과정을 생략합니다.
        if (isAlreadyTargetType(sourceValue, targetType)) {
            return sourceValue;
        }

        // 2. 먼저 등록된 사용자 정의 컨버터(registeredConverters)를 이용해 변환을 시도하고,
        final var convertedByRegistered = convertWithRegisteredConverters(sourceValue, targetType);
        if (hasConversionOccurred(convertedByRegistered, sourceValue)) {
            return convertedByRegistered;
        }

        // 3. 변환 가능한 컨버터가 없으면 기본 변환 규칙(DefaultTypeConverters)을 적용합니다.
        TypeConverter defaultConverter = DefaultTypeConverters.findConverter(sourceValue, targetType);
        if (defaultConverter != null) {
            final var converted = defaultConverter.convert(sourceValue, targetType);
            if (hasConversionOccurred(converted, sourceValue)) {
                return converted;
            }
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
            if (converter.supports(sourceValue.getClass(), targetType)) {
                return converter.convert(sourceValue, targetType);
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
