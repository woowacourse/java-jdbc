package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcCallback<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;
}
