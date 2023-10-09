package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementImpl<T> {
    T callback(PreparedStatement pstmt) throws SQLException;
}
