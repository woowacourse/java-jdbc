package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcCallback<T> {

    T call(PreparedStatement pstmt) throws SQLException;
}
