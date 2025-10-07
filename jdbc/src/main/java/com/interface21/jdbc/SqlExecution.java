package com.interface21.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecution<R> {

    R apply(final PreparedStatement preparedStatement) throws SQLException;
}
