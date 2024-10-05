package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<T, R> {

    R call(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException;
}
