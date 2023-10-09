package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementExecutor<T> {
    T execute(final PreparedStatement pstmt) throws SQLException;

}
