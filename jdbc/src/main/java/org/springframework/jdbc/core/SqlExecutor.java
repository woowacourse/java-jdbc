package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface SqlExecutor<T> {
    T execute(PreparedStatement pstmt) throws SQLException;
}
