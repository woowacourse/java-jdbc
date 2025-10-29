package com.interface21.jdbc.core.conversion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

public enum DefaultTypeConverters implements TypeConverter {

    BIGDECIMAL_TO_INT(BigDecimal.class, BigDecimal::intValue),
    BIGDECIMAL_TO_LONG(BigDecimal.class, BigDecimal::longValue),
    BIGDECIMAL_TO_DOUBLE(BigDecimal.class, BigDecimal::doubleValue),
    BIGDECIMAL_TO_FLOAT(BigDecimal.class, BigDecimal::floatValue),

    TIMESTAMP_TO_LOCALDATETIME(Timestamp.class, Timestamp::toLocalDateTime),
    DATE_TO_LOCALDATE(Date.class, v -> v.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

    private final Class<?> sourceType;
    private final Function<?, ?> converter;

    <S, T> DefaultTypeConverters(Class<S> sourceType, Function<S, T> converter) {
        this.sourceType = sourceType;
        this.converter = converter;
    }

    @Override
    public Object convertIfPossible(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (sourceType.isInstance(value)) {
            Object converted = ((Function<Object, Object>) converter).apply(value);
            if (targetType.isInstance(converted)) {
                return converted;
            }
        }
        return value;
    }

    /**
     * 주어진 sourceValue와 targetType에 적합한 변환기를 반환합니다.
     * 없으면 null을 반환합니다.
     */
    public static TypeConverter findConverter(Object sourceValue, Class<?> targetType) {
        if (sourceValue == null) return null;
        for (DefaultTypeConverters rule : values()) {
            Object converted = rule.convertIfPossible(sourceValue, targetType);
            if (targetType.isInstance(converted) && converted != sourceValue) {
                return rule;
            }
        }
        return null;
    }
}
