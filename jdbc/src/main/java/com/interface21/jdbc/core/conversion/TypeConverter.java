package com.interface21.jdbc.core.conversion;

public interface TypeConverter {
    /**
     * 변환을 지원하는 타입 조합인지 여부를 반환합니다.
     */
    boolean supports(Class<?> sourceType, Class<?> targetType);

    /**
     * 주어진 값을 targetType으로 변환합니다. supports()가 true인 경우에만 호출하도록 구현해야합니다.
     */
    Object convert(Object value, Class<?> targetType);
}
