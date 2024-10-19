package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;

public class JdbcTransactionManager {

    private final DataSource dataSource;
    private final H2SQLExceptionTranslator exceptionTranslator;

    public JdbcTransactionManager(DataSource dataSource, H2SQLExceptionTranslator exceptionTranslator) {
        this.dataSource = dataSource;
        this.exceptionTranslator = exceptionTranslator;
    }

    public JdbcTransaction getTransaction() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        JdbcTransaction transaction = new JdbcTransaction(connection, exceptionTranslator);
        transaction.begin();

        return transaction;
    }

    public void clear(JdbcTransaction transaction) {
        DataSourceUtils.releaseConnection(transaction.getConnection(), dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
