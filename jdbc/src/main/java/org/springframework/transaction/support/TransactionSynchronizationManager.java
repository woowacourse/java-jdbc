package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> resource = resources.get();
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

    public static void unbindResource(DataSource key) {
        final Map<DataSource, Connection> resource = resources.get();
        resource.remove(key);
    }

}
