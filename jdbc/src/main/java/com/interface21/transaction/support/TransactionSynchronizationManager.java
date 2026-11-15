package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var map = resources.get();
        return map == null ? null : map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        var map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("이미 연결이 등록된 DataSource입니다.");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final var map = resources.get();
        if (map == null || !map.containsKey(key)) {
            throw new IllegalStateException("등록된 연결이 없습니다.");
        }
        final var connection = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
