package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < args.length; ++parameterIndex) {
            preparedStatement.setObject(parameterIndex + 1, args[parameterIndex]);
        }
    }
}
