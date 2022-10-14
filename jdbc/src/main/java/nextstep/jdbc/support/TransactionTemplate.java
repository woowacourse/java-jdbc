package nextstep.jdbc.support;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionTemplate {

    private final PlatformTransactionManager transactionManager;

    public TransactionTemplate(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> T doTransaction(final TransactionInvoker<T> invoker) {
        final TransactionStatus transactionStatus = transactionManager
                .getTransaction(new DefaultTransactionDefinition());
        try {
            final T result = invoker.invoke();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
    }
}
