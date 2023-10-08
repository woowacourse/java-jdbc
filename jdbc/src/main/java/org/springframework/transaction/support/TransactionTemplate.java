package org.springframework.transaction.support;

import javax.sql.DataSource;

public class TransactionTemplate {
    private final TransactionManager transactionManager;

    public TransactionTemplate(final DataSource dataSource) {
        transactionManager = new TransactionManager(dataSource);
    }

    public <T> T execute(final TransactionCallback<T> transactionCallback) {
        try {
            transactionManager.startTransaction();
            T result = transactionCallback.doBizLogic();
            transactionManager.commit();
            return result;
        } catch (final RuntimeException e) {
            transactionManager.rollback();
            throw e;
        } finally {
            transactionManager.cleanUp();
        }
    }
}
