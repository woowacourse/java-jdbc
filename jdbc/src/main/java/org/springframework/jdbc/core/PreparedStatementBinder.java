package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementBinder {

    PreparedStatement bind(final PreparedStatement preparedStatement, final Object... statements) throws SQLException;
}
