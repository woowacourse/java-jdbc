package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        final Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            throw new IllegalStateException("resource is Empty");
        }
        final Map<DataSource, Connection> map = resources.get();
        return map.remove(key);
    }
}
