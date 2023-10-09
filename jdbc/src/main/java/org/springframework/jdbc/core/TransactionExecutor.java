package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
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
        } catch (Exception exception) {
            safeRollback(transaction);
            throw new DataAccessException(exception);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    private void safeRollback(Transaction transaction) {
        try {
            transaction.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public void execute(Runnable operation) {
        Transaction transaction = getTransaction();
        try {
            transaction.begin();
            operation.run();
            transaction.commit();
        } catch (Exception exception) {
            safeRollback(transaction);
            throw new DataAccessException(exception);
        } finally {
            DataSourceUtils.releaseConnectionOf(dataSource);
        }
    }

    private Transaction getTransaction() {
        return new Transaction(DataSourceUtils.getConnection(dataSource));
    }
}
