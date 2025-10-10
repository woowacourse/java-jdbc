package com.interface21.jdbc.core;

import java.util.function.Supplier;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void run(Runnable runnable) {
        transactionManager.start();
        try {
            runnable.run();
            transactionManager.commit();
        } catch (Throwable e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public <T> T run(Supplier<T> supplier) {
        transactionManager.start();
        try {
            T result = supplier.get();
            transactionManager.commit();
            return result;
        } catch (Throwable e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
