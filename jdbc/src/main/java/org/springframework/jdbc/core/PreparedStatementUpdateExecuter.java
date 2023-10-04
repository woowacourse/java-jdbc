package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementUpdateExecuter implements PreparedStatementExecuter<Integer> {

    @Override
    public Integer execute(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeUpdate();
    }
}
