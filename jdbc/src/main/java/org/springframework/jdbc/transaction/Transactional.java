package org.springframework.jdbc.transaction;

import javax.sql.DataSource;
import java.sql.Connection;

public class Transactional {

    private final TransactionManager transactionManager;

    public Transactional(DataSource dataSource) {
        this.transactionManager = new TransactionManager(dataSource);
    }

    public <T> T execute(final TransactionExecutor<T> transactionExecutor) {
        try {
            Connection connection = transactionManager.getConnection();
            transactionManager.begin(connection);
            T response = transactionExecutor.execute(connection);
            transactionManager.commit();
            return response;
        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }

}
