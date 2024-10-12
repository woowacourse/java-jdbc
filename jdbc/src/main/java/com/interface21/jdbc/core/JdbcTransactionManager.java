package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import com.interface21.dao.DataAccessException;

public class JdbcTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            executeInTransaction(consumer, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to acquire or manage database connection", e);
        }
    }

    private void executeInTransaction(Consumer<Connection> consumer, Connection connection) {
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            rollbackInTransaction(connection);
            throw new DataAccessException("Transaction failed and was rolled back", e);
        }
    }

    private void rollbackInTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Roll back failed", e);
        }
    }
}
