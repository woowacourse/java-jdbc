package org.springframework.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionManager;

import javax.sql.DataSource;

public class TransactionTemplate {

    private TransactionManager transactionManager;

    public TransactionTemplate(final DataSource dataSource) {
        this.transactionManager = new TransactionManager(dataSource);
    }

    public <T> T doTransaction(final TransactionCallback<T> transactionCallback) {
        try {
            return doInternalTransaction(() -> transactionCallback.execute());
        } catch (DataAccessException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T doInternalTransaction(final TransactionCallback<T> transactionCallback) {
        try {
            transactionManager.begin();
            final T result = transactionCallback.execute();
            transactionManager.commit();

            return result;
        } catch (DataAccessException e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        } finally {
            transactionManager.finalizeConnection();
        }
    }

    public void doTransaction(final TransactionCallbackReturnVoid transactionCallbackReturnVoid) {
        doInternalTransaction(() -> {
            transactionCallbackReturnVoid.execute();
            return null;
        });
    }
}
