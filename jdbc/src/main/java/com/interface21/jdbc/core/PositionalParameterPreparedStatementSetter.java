package com.interface21.jdbc.core;

public class PositionalParameterPreparedStatementSetter {

    public PreparedStatementSetter getPreparedStatementSetter(final Object... parameters) {
        return preparedStatement -> {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        };
    }
}
