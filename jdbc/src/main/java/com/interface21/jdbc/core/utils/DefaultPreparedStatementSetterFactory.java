package com.interface21.jdbc.core.utils;

import com.interface21.jdbc.core.PreparedStatementSetter;

public class DefaultPreparedStatementSetterFactory {

    private DefaultPreparedStatementSetterFactory() {
    }

    public static PreparedStatementSetter createDefaultPreparedStatementSetter(Object... parameters) {
        return preparedStatement -> {
            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
        };
    }
}
