package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeVoid(final Consumer<Connection> execution) {
        execute(conn -> {
            execution.accept(conn);
            return null; // void
        });
    }

    public <T> T execute(final Function<Connection, T> execution) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                throw new DataAccessException("Connection is null on dataSource " + dataSource);
            }
            return executeInTransaction(connection, execution);

        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T executeInTransaction(final Connection connection, final Function<Connection, T> execution) {
        try {
            connection.setAutoCommit(false);
            T result = execution.apply(connection);
            connection.commit();
            return result;

        } catch (final Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
