package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementFunction<T> {

    T apply(PreparedStatement pstmt) throws SQLException;
}
