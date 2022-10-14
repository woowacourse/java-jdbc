package com.techcourse.support.transaction;

import javax.sql.DataSource;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionExecutor {

    private final PlatformTransactionManager transactionManager;

    public TransactionExecutor(final DataSource dataSource) {
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final T result = callback.call();
            transactionManager.commit(transaction);

            return result;
        } catch (Exception exception) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(exception.getMessage());
        }
    }
}
