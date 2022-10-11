package nextstep.transaction;

import java.util.function.Supplier;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionTemplate {

    private final PlatformTransactionManager transactionManager;

    public TransactionTemplate(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void executeTransaction(final Runnable runnable) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            runnable.run();
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException("[ERROR] executeTransaction", e);
        }
    }

    public <T> T executeTransaction(final Supplier<T> supplier) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            T result = supplier.get();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException("[ERROR] executeTransaction", e);
        }
    }
}
