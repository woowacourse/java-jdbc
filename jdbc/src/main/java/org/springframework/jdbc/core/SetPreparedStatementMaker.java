package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetPreparedStatementMaker implements PreparedStatementMaker {

    private final String sql;
    private final Object[] args;

    public SetPreparedStatementMaker(String sql, Object[] args) {
        this.sql = sql;
        this.args = args;
    }

    @Override
    public PreparedStatement makePreparedStatement(Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setParameters(pstmt, args);
        return pstmt;
    }

    private void setParameters(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
