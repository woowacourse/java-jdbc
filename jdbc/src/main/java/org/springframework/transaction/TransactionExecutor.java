package org.springframework.transaction;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.util.function.Supplier;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        TransactionManager transactionManager = new TransactionManager(dataSource);
        try {
            transactionManager.begin();
            runnable.run();
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }

    public <R> R executeWithResult(final Supplier<R> supplier) {
        TransactionManager transactionManager = new TransactionManager(dataSource);
        try {
            transactionManager.begin();
            R result = supplier.get();
            transactionManager.commit();
            return result;
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        }
    }
}
