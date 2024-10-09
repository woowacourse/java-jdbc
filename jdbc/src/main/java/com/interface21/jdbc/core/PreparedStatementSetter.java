package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    public PreparedStatementSetter() {
    }

    public void setValues(Object[] values, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= values.length; i++) {
            preparedStatement.setObject(i, values[i - 1]);
        }
    }
}
