package org.springframework.transaction.support;

import java.util.function.Supplier;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private <T> T executeWithTransaction(Supplier<T> supplier) {
        try {
            transactionManager.createConnection();
            T result = supplier.get();
            transactionManager.commit();
            return result;
        } catch (RuntimeException ex) {
            transactionManager.rollback();
            throw ex;
        } finally {
            transactionManager.close();
        }
    }

    public void execute(Runnable runnable) {
        executeWithTransaction(
                () -> {
                    runnable.run();
                    return null;
                });
    }

    public <T> T execute(Supplier<T> supplier) {
        return executeWithTransaction(supplier);
    }
}
