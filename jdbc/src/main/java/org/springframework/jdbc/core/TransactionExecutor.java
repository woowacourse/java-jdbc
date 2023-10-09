package org.springframework.jdbc.core;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.util.function.Supplier;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(Supplier<T> operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            T result = operation.get();
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            transaction.rollback();
            throw exception;
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    public void execute(Runnable operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.run();
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
