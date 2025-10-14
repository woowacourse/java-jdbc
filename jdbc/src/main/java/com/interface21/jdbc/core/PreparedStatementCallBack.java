package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<T> {

    T doInPreparedStatement(final PreparedStatement pstmt) throws SQLException;
}
