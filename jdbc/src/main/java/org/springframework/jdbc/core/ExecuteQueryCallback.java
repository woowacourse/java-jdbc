package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecuteQueryCallback<T> {

    T execute(final PreparedStatement preparedStatement) throws SQLException;
}
