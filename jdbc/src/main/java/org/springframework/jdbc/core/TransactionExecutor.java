package org.springframework.jdbc.core;

import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionExecutor {

    private final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);
    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionSynchronizationManager.setActualTransactionActive(true);
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception exception) {
            try {
                connection.rollback();
                throw new DataAccessException(exception);
            } catch (SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
        } finally {
            try {
                TransactionSynchronizationManager.setActualTransactionActive(false);
                connection.setAutoCommit(true);
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (SQLException ignored) {
                log.warn("fail to set auto commit true due to {}", ignored.getMessage());
                log.warn(Arrays.toString(ignored.getStackTrace()));
            }
        }
    }

}
