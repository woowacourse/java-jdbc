package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class JdbcOperation<R> {

    private final DataSource dataSource;

    protected JdbcOperation(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final R execute(String sql) {
        return execute(sql, pstmt -> {
        });
    }

    public final R execute(String sql, PreparedStatementSetter setter) {
        try (Connection conn = dataSource.getConnection()) {
            return executeWithConnection(conn, sql, setter);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    public final R execute(Connection connection, String sql, PreparedStatementSetter setter) {
        try {
            return executeWithConnection(connection, sql, setter);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    private R executeWithConnection(Connection connection, String sql, PreparedStatementSetter setter)
            throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setter.setValues(pstmt);
            return executeStatement(pstmt);
        }
    }

    protected abstract R executeStatement(PreparedStatement pstmt) throws SQLException;
}
