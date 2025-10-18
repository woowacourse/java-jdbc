package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Consumer<Connection> execution) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                throw new DataAccessException("Connection is null on dataSource " + dataSource);
            }
            executeInTransaction(connection, execution);

        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void executeInTransaction(final Connection connection, final Consumer<Connection> execution) {
        try {
            connection.setAutoCommit(false);
            execution.accept(connection);
            connection.commit();

        } catch (final SQLException e) {
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
