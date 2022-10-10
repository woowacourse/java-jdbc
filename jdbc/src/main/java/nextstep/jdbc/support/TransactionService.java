package nextstep.jdbc.support;

import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public abstract class TransactionService {

    private final DataSource dataSource;
    private final TransactionDefinition transactionDefinition;

    protected TransactionService(final DataSource dataSource, final TransactionDefinition transactionDefinition) {
        this.dataSource = dataSource;
        this.transactionDefinition = transactionDefinition;
    }

    public void runWithTransaction(final Runnable runnable) {
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            runnable.run();
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
