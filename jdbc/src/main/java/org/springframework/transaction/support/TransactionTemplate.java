package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.exception.UndeclaredThrowableException;

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
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            transactionCallback.execute(connection);
            connection.commit();
        } catch (RuntimeException e) {
            if (connection != null) {
                rollback(connection);
            }
            throw e;
        } catch (Throwable e) {
            if (connection != null) {
                rollback(connection);
            }
            throw new UndeclaredThrowableException(e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
