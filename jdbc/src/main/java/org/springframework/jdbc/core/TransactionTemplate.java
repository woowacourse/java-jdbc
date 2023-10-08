package org.springframework.jdbc.core;

import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {
    private final TransactionManager transactionManager;

    public TransactionTemplate(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(final Runnable runnable) {
        try {
            transactionManager.start();

            runnable.run();

            transactionManager.commit();
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        } finally {
            transactionManager.release();
        }
    }

    public <R> R executeWithResult(final Supplier<R> supplier) {
        try {
            transactionManager.start();

            final R result = supplier.get();

            transactionManager.commit();
            return result;
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        } finally {
            transactionManager.release();
        }
    }
}
