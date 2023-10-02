package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T call(final PreparedStatement preparedStatement) throws SQLException;
}
