package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementFunction<T>{

    T execute(PreparedStatement pstmt) throws SQLException;
}
