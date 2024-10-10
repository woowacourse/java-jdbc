package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementCreator {

    private final Connection connection;
    private final String sql;

    public PreparedStatementCreator(Connection connection, String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }
        this.connection = connection;
        this.sql = sql;
    }

    public PreparedStatement create() throws SQLException {
        return connection.prepareStatement(sql);
    }
}
