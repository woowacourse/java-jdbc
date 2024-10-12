package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return Optional.ofNullable(resources.get())
                .map(resourceMap -> resourceMap.get(key))
                .orElse(null);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> resourceMap = resources.get();
        if (Objects.isNull(resourceMap)) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return Optional.ofNullable(resources.get())
                .map(resourceMap -> resourceMap.remove(key))
                .orElse(null);
    }
}
