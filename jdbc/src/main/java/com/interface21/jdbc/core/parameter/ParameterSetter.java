package com.interface21.jdbc.core.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterSetter {

    boolean isAvailableParameter(Object parameter);

    void setParameter(PreparedStatement pstmt, int index, Object parameter) throws SQLException;
}
