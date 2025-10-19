package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().putIfAbsent(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (!resources.get().containsKey(key)) {
            throw new IllegalArgumentException("해당하는 DataSource를 찾을 수 없습니다");
        }

        Connection connection = resources.get().get(key);
        resources.get().remove(key, connection);
        return connection;
    }
}
