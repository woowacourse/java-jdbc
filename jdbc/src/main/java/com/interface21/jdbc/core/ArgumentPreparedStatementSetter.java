package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {
    private static final int BASE_INDEX = 1;

    private final Object[] params;

    public ArgumentPreparedStatementSetter(Object[] params) {
        this.params = params;
    }

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {
        for(int i = 0; i < params.length; i++) {
            pstmt.setObject(i + BASE_INDEX, params[i]);
        }
    }
}
