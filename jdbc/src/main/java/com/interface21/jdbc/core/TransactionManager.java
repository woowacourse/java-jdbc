package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T start(Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            T t = supplier.get();

            connection.commit();
            return t;
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    public void start(Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}
