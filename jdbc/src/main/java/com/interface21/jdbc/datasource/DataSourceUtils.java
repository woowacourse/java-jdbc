package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public abstract class DataSourceUtils {

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        if (TransactionSynchronizationManager.hasResource(dataSource)) {
            return TransactionSynchronizationManager.getResource(dataSource);
        }

        try {
            return dataSource.getConnection();
        } catch (final SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (TransactionSynchronizationManager.hasResource(dataSource)) {
            return;
        }

        try {
            connection.close();
        } catch (final SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}
