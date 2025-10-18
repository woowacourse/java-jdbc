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
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                throw new DataAccessException("Connection is null on dataSource " + dataSource);
            }

            connection.setAutoCommit(false);
            execution.accept(connection);
            connection.commit();

        } catch (final SQLException e) {
            rollback(connection);

        } finally {
            close(connection);
        }
    }

    private void close(final Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException e) {
            rollback(connection);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new DataAccessException(ex);
        }

        throw new DataAccessException();
    }
}
