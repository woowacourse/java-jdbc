package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        return map.remove(key);
    }
}
