package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransactionWithoutResult(Runnable runnable) {
        executeTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    public <R> R executeTransaction(Callable<R> callable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            setAutoCommit(connection, false);
            R result = callable.call();
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            setAutoCommit(connection, true);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setAutoCommit(Connection connection, boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
