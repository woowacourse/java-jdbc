package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementArgumentsSetter implements PreparedStatementSetter {

    private static final int STATEMENT_ARGUMENT_OFFSET = 1;

    private final Object[] args;

    public PreparedStatementArgumentsSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        if (args == null) {
            return;
        }

        for (int argsIndex = 0; argsIndex < args.length; argsIndex++) {
            ps.setObject(argsIndex + STATEMENT_ARGUMENT_OFFSET, args[argsIndex]);
        }
    }
}
