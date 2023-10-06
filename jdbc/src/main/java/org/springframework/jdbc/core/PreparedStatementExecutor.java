package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementExecutor<T> {

    T execute(PreparedStatement psmt) throws SQLException;
}
