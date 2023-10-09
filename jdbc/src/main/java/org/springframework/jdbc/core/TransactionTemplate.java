package org.springframework.jdbc.core;

import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {
    private final TransactionManager transactionManager;

    public TransactionTemplate(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(final Runnable runnable) {
        executeInternal(() -> {
            runnable.run();
            return null;
        });
    }

    public <R> R executeWithResult(final Supplier<R> supplier) {
        return executeInternal(supplier);
    }

    private <R> R executeInternal(final Supplier<R> action) {
        try (transactionManager) {
            transactionManager.start();

            R result = action.get();

            transactionManager.commit();
            return result;
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
