package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeTransactionWithResult(Function<Connection, T> function) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            return executeTransactionWithoutResult(function, connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void executeTransactionWithoutResult(Consumer<Connection> consumer) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            executeTransactionWithoutResult(consumer, connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeTransactionWithoutResult(Function<Connection, T> function, Connection connection)
            throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = function.apply(connection);
            connection.commit();
            return result;
        } catch (Exception e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void executeTransactionWithoutResult(Consumer<Connection> consumer, Connection connection)
            throws SQLException {
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
