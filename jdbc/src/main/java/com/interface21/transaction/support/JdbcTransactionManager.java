package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcTransaction getTransaction() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        JdbcTransaction transaction = new JdbcTransaction(connection);
        transaction.begin();

        return transaction;
    }

    public void flush(JdbcTransaction transaction) {
        DataSourceUtils.releaseConnection(transaction.getConnection(), dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
