package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultParameterSetter implements ParameterSetter {

    @Override
    public void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
