package com.interface21.jdbc.core;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T, R> {

    R apply(T pstmt) throws SQLException;
}
