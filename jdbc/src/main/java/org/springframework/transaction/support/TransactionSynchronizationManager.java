package org.springframework.transaction.support;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    @Nullable
    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        final Connection oldConnection = map.put(key, value);
        if (oldConnection != null) {
            throw new IllegalStateException("Already value bound for key " + key);
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("No value bound for key " + key);
        }
        final Connection oldConnection = map.remove(key);
        if (oldConnection == null) {
            throw new IllegalStateException("No value bound for key " + key);
        }
        if (map.isEmpty()) {
            resources.remove();
        }
        return oldConnection;
    }
}
