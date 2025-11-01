package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return Optional.ofNullable(resources.get())
                .map(map -> map.get(key))
                .orElse(null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Optional.ofNullable(resources.get())
                .ifPresentOrElse(
                        map -> map.put(key, value),
                        () -> {
                            Map<DataSource, Connection> newMap = new HashMap<>();
                            newMap.put(key, value);
                            resources.set(newMap);
                        }
                );
    }

    public static void unbindResource(DataSource key) {
        Optional.ofNullable(resources.get())
                .ifPresent(map -> map.remove(key));
    }
}
