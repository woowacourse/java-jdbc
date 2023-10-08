package org.springframework.transaction.support;

import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(final DataSource dataSource) {
        this(new TransactionManager(dataSource));
    }

    TransactionTemplate(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    private <R> R run(final Supplier<R> supplier) {
        try {
            transactionManager.initialize();
            final R result = supplier.get();
            transactionManager.commit();
            return result;
        } catch (final Exception e) {
            transactionManager.rollback();
            throw new DataAccessException(e);
        } finally {
            transactionManager.close();
        }
    }

    public void execute(final Runnable transactionCallback) {
        run(() -> {
            transactionCallback.run();
            return null;
        });
    }

    public <R> R executeWithResult(final Supplier<R> transactionCallback) {
        return run(transactionCallback);
    }
}
