package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.exception.UndeclaredThrowableException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithTransaction(final TransactionCallback transactionCallback) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            transactionCallback.execute();
            connection.commit();
        } catch (final RuntimeException e) {
            if (connection != null) {
                rollback(connection);
            }
            throw e;
        } catch (final Throwable e) {
            if (connection != null) {
                rollback(connection);
            }
            throw new UndeclaredThrowableException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection);
            }
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
