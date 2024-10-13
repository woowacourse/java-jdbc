package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessException.RunAndThrowable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.sql.DataSource;

public final class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static void bindAndStartTransaction(DataSource key) {
        Connection connection = getResource(key);
        execute(() -> connection.setAutoCommit(false));
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = getDataSourceConnectionMap();
        Connection connection = dataSourceConnectionMap.get(key);
        if (connection == null || executeAndReturn(connection::isClosed)) {
            bindResource(key, executeAndReturn(key::getConnection));
            return getResource(key);
        }
        return connection;
    }

    private static <T> T executeAndReturn(Callable<T> callable) {
        return DataAccessException.executeAndConvertException(callable);
    }

    public static void unbindAndCommit(DataSource key) {
        Connection connection = unbindResource(key);
        execute(connection::commit);
    }

    private static void execute(RunAndThrowable runAndThrowable) {
        DataAccessException.executeAndConvertException(runAndThrowable);
    }

    public static void unbindAndRollback(DataSource key) {
        Connection connection = unbindResource(key);
        execute(connection::rollback);
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
