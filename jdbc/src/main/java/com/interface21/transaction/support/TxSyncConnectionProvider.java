package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.ConnectionProvider;
import java.sql.Connection;
import javax.sql.DataSource;

public class TxSyncConnectionProvider implements ConnectionProvider {

    private final DataSource dataSource;

    public TxSyncConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        return DataSourceUtils.getConnection(dataSource);
    }
}
