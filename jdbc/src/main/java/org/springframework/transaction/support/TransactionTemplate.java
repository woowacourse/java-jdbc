package org.springframework.transaction.support;

import javax.sql.DataSource;

public class TransactionTemplate {
    private final JdbcTransactionManager jdbcTransactionManager;

    public TransactionTemplate(final DataSource dataSource) {
        jdbcTransactionManager = new JdbcTransactionManager(dataSource);
    }

    public <T> T execute(final TransactionCallback<T> transactionCallback) {
        try {
            jdbcTransactionManager.startTransaction();
            T result = transactionCallback.doBizLogic();
            jdbcTransactionManager.commit();
            return result;
        } catch (final RuntimeException e) {
            jdbcTransactionManager.rollback();
            throw e;
        } finally {
            jdbcTransactionManager.cleanUp();
        }
    }

    public void executeWithoutResult(final TransactionCallbackWithoutResult transactionCallback) {
        try {
            jdbcTransactionManager.startTransaction();
            transactionCallback.doBizLogic();
            jdbcTransactionManager.commit();
        } catch (final RuntimeException e) {
            jdbcTransactionManager.rollback();
            throw e;
        } finally {
            jdbcTransactionManager.cleanUp();
        }
    }
}
