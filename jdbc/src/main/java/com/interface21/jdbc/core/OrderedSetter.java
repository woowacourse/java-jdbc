package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderedSetter implements PreparedStatementSetter {
    @Override
    public void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            preparedStatement.setObject(i, parameters[i - 1]);
        }
    }
}
