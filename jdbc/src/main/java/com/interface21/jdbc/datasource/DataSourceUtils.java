package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    /**
     * 현재 트랜잭션의 연결을 재사용하고, 없으면 새로 얻는다. 트랜잭션 등록은 호출자가 담당한다.
     */
    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        try {
            return doGetConnection(dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static Connection doGetConnection(final DataSource dataSource) throws SQLException {
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        final var connection = TransactionSynchronizationManager.getResource(dataSource);
        return connection == null ? dataSource.getConnection() : connection;
    }

    /**
     * 트랜잭션에 등록된 연결은 유지하고, 그 밖의 연결만 닫는다.
     */
    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        try {
            doReleaseConnection(connection, dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }

    public static void doReleaseConnection(final Connection connection, final DataSource dataSource)
            throws SQLException {
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        if (connection == null || connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        connection.close();
    }
}
