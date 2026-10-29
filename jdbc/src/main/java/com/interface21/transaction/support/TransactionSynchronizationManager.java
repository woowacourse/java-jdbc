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
        final Map<DataSource, Connection> map = resources.get();
        if (map.containsKey(key)) {
            throw new IllegalStateException("이미 DataSource에 바인딩된 Connection이 존재합니다.");
        }
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        final Connection connection = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
