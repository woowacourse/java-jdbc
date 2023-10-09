package org.springframework.jdbc.core;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionalOperation operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.run(transaction);
            transaction.commit();
        } catch (RuntimeException exception) {
            transaction.rollback();
            throw exception;
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    private Transaction getTransaction() {
        return new Transaction(DataSourceUtils.getConnection(dataSource));
    }
}
