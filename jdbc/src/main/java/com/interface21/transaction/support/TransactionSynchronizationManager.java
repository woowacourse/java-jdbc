package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES =
        ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return RESOURCES.get()
            .get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> resource = RESOURCES.get();
        if (resource.containsKey(key)) {
            throw new IllegalStateException("이미 연결된 바인딩된 커넥션이 존재합니다.");
        }
        resource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return RESOURCES.get()
            .remove(key);
    }
}
