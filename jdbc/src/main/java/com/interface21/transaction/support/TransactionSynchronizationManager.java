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
        Connection existing = resources.get().putIfAbsent(key, value);
        if (existing != null) {
            throw new IllegalStateException("이미 트랜잭션에 바인딩된 커넥션이 존재합니다: " + key);
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        Connection connection = resourceMap.remove(key);
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
