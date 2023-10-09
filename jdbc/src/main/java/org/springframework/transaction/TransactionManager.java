package org.springframework.transaction;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final TransactionTemplate transactionTemplate;
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionServiceExecutor<T> executor, boolean readOnly) {
        start(readOnly);
        try {
            T result = executor.execute();

            if (readOnly) {
                close();
                return result;
            }

            commit();
            return result;
        } catch (DataAccessException e) {
            rollback();
            throw e;
        }
    }

    private void start(boolean readOnly) {
        transactionTemplate.execute(connection -> {
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
        });
    }

    private void commit() {
        transactionTemplate.execute(Connection::commit);
        close();
    }

    private void close() {
        transactionTemplate.execute(connection -> DataSourceUtils.releaseConnection(connection, dataSource));
    }

    private void rollback() {
        transactionTemplate.execute(Connection::rollback);
        close();
    }

}
