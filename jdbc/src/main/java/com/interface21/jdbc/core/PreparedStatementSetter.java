package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    int PARAMETER_START_INDEX = 1;

    void setValues(PreparedStatement ps) throws SQLException;
}
