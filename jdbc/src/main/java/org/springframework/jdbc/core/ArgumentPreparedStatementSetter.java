package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter {

    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object... args) {
        this.args = args;
    }

    public void setValues(PreparedStatement pstmt) throws SQLException {
        int setValueIdx = 1;
        for (Object arg : args) {
            pstmt.setObject(setValueIdx++, arg);
        }
    }
}
