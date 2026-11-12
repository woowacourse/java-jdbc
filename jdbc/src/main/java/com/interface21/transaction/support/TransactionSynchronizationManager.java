package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("이미 바인딩된 Connection이 있습니다. dataSource=" + key);
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        final Connection connection = map.remove(key);
        // 스레드 풀에서 스레드가 재사용될 때 이전 요청의 값이 남지 않도록 비어 있으면 ThreadLocal을 정리한다.
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
