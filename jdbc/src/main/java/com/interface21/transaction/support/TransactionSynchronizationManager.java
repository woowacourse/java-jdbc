package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            return null;
        }
        return resourceMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            resourceMap = new HashMap<>();
            resources.set(resourceMap);
        }
        if (resourceMap.containsKey(key)) {
            throw new IllegalStateException("이미 등록된 데이터소스입니다.");
        }
        resourceMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            return null;
        }
        Connection targetConnection = resourceMap.remove(key);
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
        return targetConnection;
    }
}
