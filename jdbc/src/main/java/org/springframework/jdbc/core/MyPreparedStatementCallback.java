package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface MyPreparedStatementCallback {

    void execute(PreparedStatement pstmt) throws SQLException;

}
