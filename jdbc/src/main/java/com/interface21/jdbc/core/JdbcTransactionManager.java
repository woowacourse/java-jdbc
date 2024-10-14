package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable runnable) {
        executeInTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T execute(Supplier<T> supplier) {
        return executeInTransaction(supplier);
    }

    private <T> T executeInTransaction(Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (Exception e) {
            rollbackInTransaction(connection);
            throw new DataAccessException("Transaction failed and was rolled back", e);
        } finally {
            setAutoCommit(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollbackInTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Roll back failed", e);
        }
    }

    private void setAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("Fail to set auto commit to true", e);
        }
    }
}
