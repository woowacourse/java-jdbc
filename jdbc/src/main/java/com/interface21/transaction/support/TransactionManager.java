package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void performTransaction(Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            rollback(connection);
            throw new DataAccessException("Failed to perform transaction", exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T performTransaction(Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            rollback(connection);
            throw new DataAccessException("Failed to perform transaction", exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);

            throw new DataAccessException("Failed to rollback", exception);
        }
    }
}
