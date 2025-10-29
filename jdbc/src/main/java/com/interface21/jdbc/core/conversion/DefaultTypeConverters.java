package com.interface21.jdbc.core.conversion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

public enum DefaultTypeConverters implements TypeConverter {

    BIGDECIMAL_TO_INT(BigDecimal.class, Integer.class, BigDecimal::intValue),
    BIGDECIMAL_TO_LONG(BigDecimal.class, Long.class, BigDecimal::longValue),
    BIGDECIMAL_TO_DOUBLE(BigDecimal.class, Double.class, BigDecimal::doubleValue),
    BIGDECIMAL_TO_FLOAT(BigDecimal.class, Float.class, BigDecimal::floatValue),

    TIMESTAMP_TO_LOCALDATETIME(Timestamp.class, java.time.LocalDateTime.class, Timestamp::toLocalDateTime),
    DATE_TO_LOCALDATE(Date.class, java.time.LocalDate.class, v -> v.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

    private final Class<?> sourceType;
    private final Class<?> targetType;
    private final Function<?, ?> converter;

    <S, T> DefaultTypeConverters(Class<S> sourceType, Class<T> targetType, Function<S, T> converter) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.converter = converter;
    }

    @Override
    public boolean supports(Class<?> sourceType, Class<?> targetType) {
        // 요청 sourceType이 rule의 sourceType(생산자)의 같거나 하위 타입인지
        boolean sourceOk = this.sourceType.isAssignableFrom(sourceType);

        // 요청 targetType이 rule이 생산하는 targetType을 수용할 수 있는지
        boolean targetOk = targetType.isAssignableFrom(this.targetType);

        return sourceOk && targetOk;
    }


    @Override
    public Object convert(Object value, Class<?> targetType) {
        if (value == null) return null;
        return ((Function<Object, Object>) converter).apply(value);
    }

    /**
     * 주어진 sourceValue와 targetType에 적합한 변환기를 반환합니다.
     * 없으면 null을 반환합니다.
     */
    public static TypeConverter findConverter(Object sourceValue, Class<?> targetType) {
        if (sourceValue == null) return null;
        for (DefaultTypeConverters rule : values()) {
            Object converted = rule.convert(sourceValue, targetType);
            if (targetType.isInstance(converted) && converted != sourceValue) {
                return rule;
            }
        }
        return null;
    }
}
