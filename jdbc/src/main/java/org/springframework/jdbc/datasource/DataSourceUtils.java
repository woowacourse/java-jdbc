package org.springframework.jdbc.datasource;

import org.springframework.jdbc.datasource.exception.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.exception.InvalidReleaseConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    public static Connection getConnection(final DataSource dataSource) {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection == null) {
            connection = getNewConnection(dataSource);
        }

        return connection;
    }

    private static Connection getNewConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();

            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("JDBC Connection을 얻지 못했습니다.", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        try {
            if (connection.getAutoCommit()) {
                final Connection unbindConnection = TransactionSynchronizationManager.unbindResource(dataSource);

                if (connection != unbindConnection) {
                    throw new InvalidReleaseConnectionException("사용하던 Connection이 아닙니다.");
                }

                connection.close();
                return ;
            }

            final Connection bindConnection = TransactionSynchronizationManager.getResource(dataSource);

            if (connection == bindConnection) {
                return ;
            }

            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (final SQLException ex) {
            throw new CannotGetJdbcConnectionException("JDBC Connection을 닫지 못했습니다.", ex);
        }
    }

    private DataSourceUtils() {
    }
}
