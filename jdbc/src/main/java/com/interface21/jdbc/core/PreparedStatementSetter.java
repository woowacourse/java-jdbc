package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter<T> {

    T setValues(PreparedStatement preparedStatement) throws SQLException;
}
