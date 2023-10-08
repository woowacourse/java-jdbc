package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutor<T> {
    T execute(final ResultSet rs) throws SQLException;

}
