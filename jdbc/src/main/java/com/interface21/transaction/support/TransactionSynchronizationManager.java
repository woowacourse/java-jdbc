package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public final class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getTransactionStartedResource(DataSource key) {
        Connection connection = getResource(key);
        startTransaction(connection);
        return connection;
    }

    private static void startTransaction(Connection connection){
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = getDataSourceConnectionMap();
        Connection connection = dataSourceConnectionMap.get(key);
        if (connection == null || isClosed(connection)) {
            bindResource(key, getConnection(key));
            return getResource(key);
        }
        return connection;
    }

    private static boolean isClosed(Connection connection){
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static Connection getConnection(DataSource key){
        try {
            return key.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void unbindAndCommit(DataSource key) {
        Connection connection = unbindResource(key);
        commit(connection);
    }

    private static void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void unbindAndRollback(DataSource key) {
        Connection connection = unbindResource(key);
        rollback(connection);
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }


    private static Map<DataSource, Connection> getDataSourceConnectionMap() {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            resources.set(new HashMap<>());
            dataSourceConnectionMap = resources.get();
        }
        return dataSourceConnectionMap;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = getDataSourceConnectionMap();
        dataSourceConnectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = getDataSourceConnectionMap();
        return dataSourceConnectionMap.remove(key);
    }
}
