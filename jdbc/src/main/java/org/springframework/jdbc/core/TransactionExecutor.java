package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionalOperationWithReturn<T> operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            T result = operation.execute();
            transaction.commit();
            return result;
        } catch (Exception exception) {
            transaction.rollback();
            throw new DataAccessException(exception);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    public void execute(TransactionalOperation operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.execute();
            transaction.commit();
        } catch (Exception exception) {
            transaction.rollback();
            throw new DataAccessException(exception);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    private Transaction getTransaction() {
        return new Transaction(DataSourceUtils.getConnection(dataSource));
    }
}
