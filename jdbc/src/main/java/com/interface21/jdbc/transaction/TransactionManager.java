package com.interface21.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public abstract class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void processInTransaction(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            close(connection);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void close(@Nullable Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
