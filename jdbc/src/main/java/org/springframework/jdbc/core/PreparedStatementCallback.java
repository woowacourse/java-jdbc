package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallback<T> {

    T doPreparedStatement(PreparedStatement pstmt) throws SQLException;
    String getSql();
}
