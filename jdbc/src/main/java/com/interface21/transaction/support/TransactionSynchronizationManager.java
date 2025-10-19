package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) throws SQLException {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        return resource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            resource = new HashMap<>();
            resources.set(resource);
        }
        resource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        Connection value = resource.remove(key);
        if (resource.isEmpty()) {
            resources.remove();
        }
        return value;
    }
}
