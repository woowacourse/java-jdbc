package org.springframework.jdbc.core;

import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface StatementExecutor<T> {

    T execute(Statement statement) throws SQLException;
}
