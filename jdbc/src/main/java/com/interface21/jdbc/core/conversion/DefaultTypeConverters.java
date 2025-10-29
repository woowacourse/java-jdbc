package com.interface21.jdbc.core.conversion;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

public enum DefaultTypeConverters implements TypeConverter {

    BIGDECIMAL_TO_INT(BigDecimal.class, Integer.class, v -> ((BigDecimal)v).intValue()),
    BIGDECIMAL_TO_LONG(BigDecimal.class, Long.class, v -> ((BigDecimal)v).longValue()),
    BIGDECIMAL_TO_DOUBLE(BigDecimal.class, Double.class, v -> ((BigDecimal)v).doubleValue()),
    BIGDECIMAL_TO_FLOAT(BigDecimal.class, Float.class, v -> ((BigDecimal)v).floatValue()),

    TIMESTAMP_TO_LOCALDATETIME(Timestamp.class, LocalDateTime.class, v -> ((Timestamp)v).toLocalDateTime()),
    DATE_TO_LOCALDATE(Date.class, LocalDate.class, v -> ((Date)v).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

    private final Class<?> source;
    private final Class<?> target;
    private final Function<Object, Object> converter;

    DefaultTypeConverters(Class<?> source, Class<?> target, Function<Object, Object> converter) {
        this.source = source;
        this.target = target;
        this.converter = converter;
    }

    @Override
    public Object convertIfPossible(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (source.isInstance(value) && target == targetType) {
            return converter.apply(value);
        }
        return value;
    }

    public static Object applyDefaultConversion(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        for (DefaultTypeConverters rule : values()) {
            if (rule.source.isInstance(value) && rule.target == targetType) {
                return rule.converter.apply(value);
            }
        }
        return value;
    }
}
