package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

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
        boolean alreadyBound = TransactionSynchronizationManager.getResource(dataSource) != null;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        if (alreadyBound) {
            return executeWithBoundConnection(connection, sql, setter);
        }

        try (connection) {
            return executeWithConnection(connection, sql, setter);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private R executeWithBoundConnection(Connection connection, String sql, PreparedStatementSetter setter) {
        try {
            return executeWithConnection(connection, sql, setter);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    protected abstract R executeStatement(PreparedStatement pstmt) throws SQLException;

    private R executeWithConnection(Connection connection, String sql, PreparedStatementSetter setter)
            throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setter.setValues(pstmt);
            return executeStatement(pstmt);
        }
    }
}
