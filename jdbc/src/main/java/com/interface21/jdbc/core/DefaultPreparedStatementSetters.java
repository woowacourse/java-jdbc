package com.interface21.jdbc.core;

public class DefaultPreparedStatementSetters {

    public PreparedStatementSetter getPreparedStatementSetter(Object... parameters) {
        return preparedStatement -> {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        };
    }
}
