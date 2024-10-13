package com.interface21.jdbc.core.utils;

import com.interface21.jdbc.core.PreparedStatementSetter;

public class DefaultPreparedStatementSetterFactory {

    private DefaultPreparedStatementSetterFactory() {
    }

    public static PreparedStatementSetter createDefaultPreparedStatementSetter(Object... parameters) {
        return preparedStatement -> {
            int statementParametersAmount = preparedStatement.getParameterMetaData().getParameterCount();

            if (statementParametersAmount != parameters.length) {
                throw new IllegalArgumentException("파라미터 개수가 잘못되었습니다. 의도된 개수: " + statementParametersAmount);
            }

            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
        };
    }
}
