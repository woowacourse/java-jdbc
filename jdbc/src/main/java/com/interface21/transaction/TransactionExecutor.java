package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> execution) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                throw new DataAccessException("Connection is null on dataSource " + dataSource);
            }

            connection.setAutoCommit(false);
            execution.accept(connection);
            connection.commit();

        } catch (SQLException e) {
            rollback(connection);

        } finally {
            close(connection);
        }
    }

    private void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            rollback(connection);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }

        throw new DataAccessException();
    }
}
