package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallBack<T> {

    T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;

}
