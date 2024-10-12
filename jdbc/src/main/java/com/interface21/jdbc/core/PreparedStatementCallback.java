package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<R> {

    R doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
}