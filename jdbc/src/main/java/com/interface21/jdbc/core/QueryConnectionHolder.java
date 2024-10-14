package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class QueryConnectionHolder {

    private final String query;
    private final Connection connection;

    public QueryConnectionHolder(Connection connection, String query) {
        this.connection = Objects.requireNonNull(connection);
        this.query = Objects.requireNonNull(query);
    }

    public PreparedStatement getAsPreparedStatement() {
        try {
            return connection.prepareStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("PreparedStatement 생성 실패", e);
        }
    }
}
