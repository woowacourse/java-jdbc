package com.techcourse.service;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        TransactionSynchronizationManager.startNewTransaction(dataSource);

        final T result = callback.execute();

        TransactionSynchronizationManager.finishTransaction(dataSource);

        return result;
    }
}
