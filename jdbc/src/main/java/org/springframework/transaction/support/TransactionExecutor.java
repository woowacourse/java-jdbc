package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    private final DataSource dataSource;
    private Connection connection;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable action) {
        try {
            transactionStart();
            action.run();
            transactionCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            transactionRollback();
            throw e;
        } finally {
            DataSourceUtils.releaseTransactionConnection(connection, dataSource);
        }
    }

    public <T> T execute(Supplier<T> action) {
        try {
            transactionStart();
            final T returnValue = action.get();
            transactionCommit();
            return returnValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            transactionRollback();
            throw e;
        } finally {
            DataSourceUtils.releaseTransactionConnection(connection, dataSource);
        }
    }

    private void transactionStart() throws SQLException {
        this.connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        log.info("transaction start");
    }

    private void transactionCommit() throws SQLException {
        connection.commit();
        log.info("transaction commit");
    }

    private void transactionRollback() {
        try {
            connection.rollback();
            log.info("transaction rollback");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
