package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private static final int INIT_PARAMETER_INDEX = 1;

    private final Object[] argument;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.argument = args;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        for (int index = 0; index < argument.length; index++) {
            preparedStatement.setObject(index + INIT_PARAMETER_INDEX, argument[index]);
        }
    }
}
