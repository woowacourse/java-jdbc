package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementCreator {

    public PreparedStatement createPreparedStatement(final Connection conn, final String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
