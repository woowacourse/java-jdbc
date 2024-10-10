package com.interface21.jdbc.core;

import static java.util.Objects.requireNonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private final Object[] args;

    public PreparedStatementSetter(final Object[] args) {
        requireNonNull(args, "args must not be null");
        this.args = args;
    }

    public void setValues(final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
