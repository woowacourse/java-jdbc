package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryExecutor<T> {

    T execute(PreparedStatement pstmt) throws SQLException;

}
