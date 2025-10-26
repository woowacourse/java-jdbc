package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        return resource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resource = new HashMap<>();
        if (resources.get().containsKey(key)) {
            throw new IllegalStateException("이미 연결된 바인딩된 커넥션이 존재합니다.");
        }
        resource.put(key, value);
        resources.set(resource);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        return resource.remove(key);
    }
}
