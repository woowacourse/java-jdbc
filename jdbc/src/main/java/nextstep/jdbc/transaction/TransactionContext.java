package nextstep.jdbc.transaction;

import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionContext {

    private final PlatformTransactionManager transactionManager;

    public TransactionContext(final PlatformTransactionManager transactionManager) {
        this.transactionManager  = transactionManager;
    }

    public <T> T runFunction(final TransactionFunction<T> function) {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            T result =  function.execute();
            transactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException();
        }
    }

    public void runConsumer(final TransactionConsumer consumer) {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            consumer.consume();
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException();
        }
    }
}
