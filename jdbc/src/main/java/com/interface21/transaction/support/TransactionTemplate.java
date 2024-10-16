package com.interface21.transaction.support;

import com.interface21.transaction.PlatformTransactionManager;

public class TransactionTemplate {

    private final PlatformTransactionManager transactionManager;

    public TransactionTemplate(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> T execute(TransactionCallback<T> action) {
        TransactionHolder transaction = transactionManager.startTransaction();
        T result;
        try {
            result = action.doInTransaction();
        } catch (RuntimeException e) {
            transactionManager.rollback(transaction);
            throw e;
        }
        transactionManager.commit(transaction);
        return result;
    }
}
