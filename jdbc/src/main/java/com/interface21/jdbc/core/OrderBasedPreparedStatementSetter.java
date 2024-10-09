package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderBasedPreparedStatementSetter implements PreparedStatementSetter {

    private final Object[] values;

    public OrderBasedPreparedStatementSetter(Object... values) {
        this.values = values;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + PARAMETER_START_INDEX, values[i]);
        }
    }
}
