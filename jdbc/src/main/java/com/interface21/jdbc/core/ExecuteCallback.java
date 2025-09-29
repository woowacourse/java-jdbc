package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ExecuteCallback<T> {

    T call(PreparedStatement pstmt) throws SQLException;
}
