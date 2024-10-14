package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(Runnable consumer) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            executeInTransaction(connection, consumer);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void executeInTransaction(Connection connection, Runnable consumer) throws SQLException {
        try {
            connection.setAutoCommit(false);
            consumer.run();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    public <T> T getResultInTransaction(Supplier<T> supplier) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            return getResultInTransaction(connection, supplier);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private  <T> T getResultInTransaction(Connection connection, Supplier<T> supplier) throws SQLException {
        try {
            connection.setAutoCommit(false);
            return supplier.get();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }
}
