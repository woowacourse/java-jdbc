package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface MyPreparedStatementCallback {

    void execute(PreparedStatement pstmt) throws SQLException;

}
