package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var sources = resources.get();
        if (sources == null) {
            return null;
        }
        return sources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var sources = resources.get();
        if (sources == null) {
            sources = new HashMap<>();
        }
        sources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final var sources = resources.get();
        if (sources == null) {
            return null;
        }
        return sources.remove(key);
    }
}
