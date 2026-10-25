package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(final Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            setValue(preparedStatement, i + 1, args[i]);
        }
    }

    private void setValue(final PreparedStatement preparedStatement, final int index, final Object arg)
            throws SQLException {
        if (arg == null) {
            preparedStatement.setNull(index, Types.NULL);
            return;
        }
        preparedStatement.setObject(index, arg);
    }
}
