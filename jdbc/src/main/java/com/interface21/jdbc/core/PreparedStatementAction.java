package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementAction<T> {

    T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
}
