package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var resource = resources.get();
        return resource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final var resource = resources.get();
        resource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final var resource = resources.get();
        return resource.remove(key);
    }
}
