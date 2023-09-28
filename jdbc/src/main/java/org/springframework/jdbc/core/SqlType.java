package org.springframework.jdbc.core;

import java.util.Arrays;

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
                .orElseThrow(SqlTypeException::new);
    }
}
