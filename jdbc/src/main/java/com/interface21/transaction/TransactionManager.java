package com.interface21.transaction;

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

    public <T> T executeTransaction(Function<Connection, T> function) {
        try (Connection connection = dataSource.getConnection()) {
            return executeTransaction(function, connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void executeTransaction(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            executeTransaction(consumer, connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeTransaction(Function<Connection, T> function, Connection connection) throws SQLException {
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

    private void executeTransaction(Consumer<Connection> consumer, Connection connection) throws SQLException {
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
