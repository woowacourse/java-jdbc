package org.springframework.transaction.support;

import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionTemplate(final DataSource dataSource) {
        this.transactionManager = new TransactionManager(dataSource);
    }

    public <T> T execute(final Supplier<T> supplier) {
        try {
            transactionManager.begin();
            final T t = supplier.get();
            transactionManager.commit();
            return t;
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            transactionManager.clear();
        }
    }

    public void execute(final Runnable runnable) {
        try {
            transactionManager.begin();
            runnable.run();
            transactionManager.commit();
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            transactionManager.clear();
        }
    }
}
