package com.interface21.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    private static final int SET_OBJECT_BASE = 1;

    public void setValue(PreparedStatement psmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            psmt.setObject(i + SET_OBJECT_BASE, args[i]);
        }
    }
}
