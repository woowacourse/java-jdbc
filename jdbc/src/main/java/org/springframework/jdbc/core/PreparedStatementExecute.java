package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementExecute<T> {

    T callback(final PreparedStatement pstmt) throws SQLException;
}
