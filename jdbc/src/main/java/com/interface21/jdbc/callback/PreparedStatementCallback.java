package com.interface21.jdbc.callback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T run(PreparedStatement pstmt) throws SQLException;
}
