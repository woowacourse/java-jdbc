package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public enum ParameterSetter {
    LONG_SETTER(Long.class, ((preparedStatement, parameterIndex, parameter) -> {
        preparedStatement.setLong(parameterIndex, (Long) parameter);
    })),
    STRING_SETTER(String.class, ((preparedStatement, parameterIndex, parameter) -> {
        preparedStatement.setString(parameterIndex, (String) parameter);
    }));

    private final Class<?> type;
    private final ParameterConsumer parameterConsumer;

    ParameterSetter(Class<?> type, ParameterConsumer parameterConsumer) {
        this.type = type;
        this.parameterConsumer = parameterConsumer;
    }

    public static void apply(PreparedStatement preparedStatement, int parameterIndex, Object parameter) {
        Arrays.stream(values())
                .filter(setter -> setter.type.equals(parameter.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않은 타입입니다. [%s]".formatted(parameter.getClass())))
                .setParameter(preparedStatement, parameterIndex, parameter);
    }

    private void setParameter(PreparedStatement preparedStatement, int parameterIndex, Object parameter) {
        try {
            this.parameterConsumer.apply(preparedStatement, parameterIndex, parameter);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
