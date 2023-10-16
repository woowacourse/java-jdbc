package org.springframework.jdbc.datasource;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.support.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static ConnectionHolder getConnectionHolder(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (TransactionSynchronizationManager.isTransactionBegan()) {
            return ConnectionHolder.activeTransaction(connection);
        }

        return ConnectionHolder.disableTransaction(connection);
    }
}
