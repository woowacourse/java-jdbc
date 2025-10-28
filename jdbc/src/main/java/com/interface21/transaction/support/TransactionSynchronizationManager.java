package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final var changed = resources.get();
        changed.put(key, value);
        resources.set(changed);
    }

    public static Connection unbindResource(DataSource key) {
        final var changed = resources.get();
        final var removed = changed.remove(key);
        resources.set(changed);
        return removed;
    }
}
