package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(Function<Connection, T> action) {
        try (Connection connection = dataSource.getConnection()) {
            return start(action, connection);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public void execute(Consumer<Connection> action) {
        execute(connection -> {
            action.accept(connection);
            return null;
        });
    }

    private <T> T start(Function<Connection, T> action, Connection connection) throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = action.apply(connection);
            connection.commit();
            return result;
        } catch (DataAccessException | SQLException exception) {
            connection.rollback();
            throw new DataAccessException(exception);
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
