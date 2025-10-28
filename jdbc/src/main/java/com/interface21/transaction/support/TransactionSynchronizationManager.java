package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        if (getResource(key) != null) {
            throw new IllegalStateException("Already bound resource, key: " + key);
        }

        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            // 쓰레드 로컬에 커넥션을 관리하는 Map이 없는 경우 생성
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("Not bound resource, key: " + key);
        }

        final Connection value = map.remove(key);
        if (value == null) {
            throw new IllegalStateException("Not bound resource, key: " + key);
        }

        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }
}
