package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
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

    public void performTransaction(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            performTransaction(connection, consumer);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);

            throw new DataAccessException(exception);
        }
    }

    public <R> R performTransaction(Function<Connection, R> function) {
        try (Connection connection = dataSource.getConnection()) {
            return performTransaction(connection, function);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);

            throw new DataAccessException(exception);
        }
    }

    private void performTransaction(Connection connection, Consumer<Connection> consumer) throws SQLException {
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            connection.rollback();
            throw new DataAccessException(exception);
        }
    }

    private <R> R performTransaction(Connection connection, Function<Connection, R> function) throws SQLException {
        try {
            connection.setAutoCommit(false);
            R result = function.apply(connection);
            connection.commit();
            return result;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);

            connection.rollback();
            throw new DataAccessException(exception);
        }
    }
}
