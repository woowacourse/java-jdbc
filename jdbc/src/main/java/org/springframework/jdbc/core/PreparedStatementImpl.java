package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementImpl<T> {
    T implement(PreparedStatement pstmt) throws SQLException;
}
