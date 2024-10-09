package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultParameterSetter implements ParameterSetter {

    private final Object[] args;

    public DefaultParameterSetter(Object... args) {
        this.args = args;
    }

    @Override
    public void setParameters(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
