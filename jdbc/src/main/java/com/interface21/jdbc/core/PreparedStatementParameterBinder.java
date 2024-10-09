package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementParameterBinder {
    public static void bindStatementParameters(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
