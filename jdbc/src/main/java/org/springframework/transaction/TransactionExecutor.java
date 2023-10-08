package org.springframework.transaction;

import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

public class TransactionExecutor {

    private final TransactionManager transactionManager;

    public TransactionExecutor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(final Runnable runnable) {
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
