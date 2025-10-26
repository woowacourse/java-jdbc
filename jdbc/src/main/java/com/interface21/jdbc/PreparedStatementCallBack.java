package com.interface21.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<T> {

    T run(PreparedStatement pstmt) throws SQLException;
}
