package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementCreator {

    private final String sql;

    public PreparedStatementCreator(String sql) {
        this.sql = sql;
    }

    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }

}
