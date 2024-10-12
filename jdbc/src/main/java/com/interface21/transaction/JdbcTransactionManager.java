package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T doInTransaction(Function<Connection, T> action) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            T result = action.apply(connection);

            connection.commit();
            connection.setAutoCommit(true);
            return result;
        } catch (Exception e) {
            throw new DataAccessException("Transaction failed", e);
        }
    }

    @Override
    public void doInTransaction(Consumer<Connection> action) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            action.accept(connection);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new DataAccessException("Transaction failed", e);
        }
    }
}
