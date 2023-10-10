package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementAction<T> {

    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
