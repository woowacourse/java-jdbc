package org.springframework.jdbc.core;

import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplateException.NoSqlTypeException;

public enum SqlType {

    STRING(String.class),
    INT(Integer.class),
    LONG(Long.class),
    BOOLEAN(Boolean.class);

    private final Class<?> type;

    SqlType(final Class<?> type) {
        this.type = type;
    }

    static SqlType get(final Object value) {
        return Arrays.stream(values())
                .filter(sqlType -> sqlType.type.equals(value.getClass()))
                .findFirst()
                .orElseThrow(() -> new NoSqlTypeException("type: " + value.getClass().getSimpleName()));
    }

    static SqlType get(final Class<?> value) {
        return Arrays.stream(values())
                .filter(sqlType -> sqlType.type.equals(value))
                .findFirst()
                .orElseThrow(() -> new NoSqlTypeException("type: " + value.getSimpleName()));
    }
}
