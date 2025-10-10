package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] params;

    public ArgumentPreparedStatementSetter(final Object... params) {
        this.params = params;
    }

    @Override
    public void setParameters(final PreparedStatement pstmt) throws SQLException {
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i+1, params[i]);
        }
    }
}
