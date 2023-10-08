package org.springframework.transaction.core;

import org.springframework.transaction.support.ConnectionHolder;
import org.springframework.transaction.support.TransactionExecutor;
import org.springframework.transaction.support.TransactionManager;

import javax.sql.DataSource;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(DataSource dataSource) {
        this.transactionManager = new TransactionManager(dataSource, ConnectionHolder.getInstance());
    }

    public <T> T execute(TransactionExecutor<T> transactionExecutor) {
        try {
            transactionManager.start();
            T result = transactionExecutor.execute();
            transactionManager.commit();
            return result;
        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
