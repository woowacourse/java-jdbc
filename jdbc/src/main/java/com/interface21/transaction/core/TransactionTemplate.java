package com.interface21.transaction.core;

import com.interface21.dao.DataAccessException;

public class TransactionTemplate {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionTemplate(final PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        try {
            platformTransactionManager.init();
            final T result = callback.doInTransaction();
            platformTransactionManager.commit();
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback();
            throw new DataAccessException("Transaction failed and rolled back", e);
        }
    }
}
