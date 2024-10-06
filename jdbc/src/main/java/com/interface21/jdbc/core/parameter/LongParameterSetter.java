package com.interface21.jdbc.core.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongParameterSetter implements ParameterSetter {
    @Override
    public boolean isAvailableParameter(Object parameter) {
        return parameter instanceof Long;
    }

    @Override
    public void setParameter(PreparedStatement pstmt, int index, Object parameter) throws SQLException {
        pstmt.setLong(index, (Long) parameter);
    }
}
