package org.springframework.transaction;

import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

public class TransactionExecutor {

    private final TransactionManager transactionManager;

    public TransactionExecutor(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(final Runnable runnable) {
        executeTemplate(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T executeTemplate(final Supplier<T> supplier) {
        try {
            transactionManager.begin();
            final T result = supplier.get();
            transactionManager.commit();
            return result;
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        } finally {
            transactionManager.close();
        }
    }
}
