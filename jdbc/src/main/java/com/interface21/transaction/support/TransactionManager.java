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
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            performTransaction(connection, runnable);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);

            throw new DataAccessException(exception);
        }
    }

    public <T> T performTransaction(Supplier<T> supplier) {
        try (Connection connection = dataSource.getConnection()) {
            return performTransaction(connection, supplier);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);

            throw new DataAccessException(exception);
        }
    }

    private void performTransaction(Connection connection, Runnable runnable) throws SQLException {
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            connection.rollback();
            throw new DataAccessException(exception);
        }
    }

    private <T> T performTransaction(Connection connection, Supplier<T> supplier) throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            connection.rollback();
            throw new DataAccessException(exception);
        }
    }
}
