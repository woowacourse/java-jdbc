package nextstep.jdbc.support;

import nextstep.jdbc.exception.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class TransactionService {

    private final PlatformTransactionManager transactionManager;

    protected TransactionService(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void runWithTransaction(final Runnable runnable) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            runnable.run();
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
