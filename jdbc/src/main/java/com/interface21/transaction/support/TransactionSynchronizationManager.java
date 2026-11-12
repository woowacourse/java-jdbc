package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "데이터소스는 필수입니다.");
        Objects.requireNonNull(value, "연결은 필수입니다.");
        if (getResource(key) != null) {
            throw new IllegalStateException("이미 연결이 바인딩되어 있습니다.");
        }
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final var connections = resources.get();
        if (connections == null || !connections.containsKey(key)) {
            throw new IllegalStateException("바인딩된 연결이 없습니다.");
        }
        final var connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
