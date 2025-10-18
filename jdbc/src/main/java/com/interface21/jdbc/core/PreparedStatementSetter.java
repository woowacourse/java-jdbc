package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {
    void setValues(PreparedStatement pstmt) throws SQLException;

    static PreparedStatementSetter ofSequenced(final Object... args) {
        return pstmt -> {
            for (int i = 1; i <= args.length; i++) {
                pstmt.setObject(i, args[i - 1]);
            }
        };
    }
}
