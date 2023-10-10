package org.springframework.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = init();

    private TransactionSynchronizationManager() {}

    private static ThreadLocal<Map<DataSource, Connection>> init() {
        final ThreadLocal<Map<DataSource, Connection>> result = new ThreadLocal<>();
        result.set(new HashMap<>());
        return result;
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        return map.remove(key);
    }
}
