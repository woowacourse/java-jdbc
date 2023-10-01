package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor<T> {
    T execute(PreparedStatement pstmt) throws SQLException;
}
