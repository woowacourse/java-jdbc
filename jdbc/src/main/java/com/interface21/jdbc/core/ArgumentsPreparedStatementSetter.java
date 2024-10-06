package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentsPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] args;

    public ArgumentsPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        if (args == null) {
            return;
        }
        for (int statementIndex = 1, argsIndex = 0; argsIndex < args.length; statementIndex++, argsIndex++) {
            ps.setObject(statementIndex, args[argsIndex]);
        }
    }
}
