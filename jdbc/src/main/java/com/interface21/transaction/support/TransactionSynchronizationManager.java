package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Connection connection = resources.get()
                .get(key);
        if (resources.get() == null || isClosed(connection)) {
            return null;
        }
        return connection;
    }

    private static boolean isClosed(final Connection connection) {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get()
                .put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get()
                .remove(key);
    }
}
