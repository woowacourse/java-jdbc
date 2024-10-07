package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] arguments;

    public ArgumentPreparedStatementSetter(Object... arguments) {
        this.arguments = arguments;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) {
        try {
            for (int argumentIndex = 0; argumentIndex < arguments.length; argumentIndex++) {
                preparedStatement.setObject(argumentIndex + 1, arguments[argumentIndex]);
            }
        } catch (SQLException e) {
            throw new JdbcException("An error occured during setting values", e);
        }
    }
}
