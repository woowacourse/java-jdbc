package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =
            ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return resources.get().getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key and value must not be null");
        }
        final Map<DataSource, Connection> map = resources.get();
        Connection existing = map.putIfAbsent(key, value);
        if (existing != null) {
            throw new IllegalStateException("이미 다른 커넥션이 바인딩되어 있습니다.");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        final Connection removed = map.remove(key);

        return removed;
    }
}
