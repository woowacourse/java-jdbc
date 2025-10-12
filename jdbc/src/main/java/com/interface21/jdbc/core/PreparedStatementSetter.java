package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement pstmt) throws SQLException;

    static PreparedStatementSetter of(Object... params) {
        return pstmt -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }
}
