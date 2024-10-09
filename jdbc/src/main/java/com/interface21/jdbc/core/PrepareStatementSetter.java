package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PrepareStatementSetter {

    void setValues(PreparedStatement preparedStatement, Object... objects) throws SQLException;
}
