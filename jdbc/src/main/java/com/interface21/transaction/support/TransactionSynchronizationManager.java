package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Map<DataSource, Integer>> transactionDepth = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
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
        map.put(key, value);

        Map<DataSource, Integer> depthMap = transactionDepth.get();
        if (depthMap == null) {
            depthMap = new HashMap<>();
            transactionDepth.set(depthMap);
        }
        int currentDepth = depthMap.getOrDefault(key, 0);
        depthMap.put(key, currentDepth + 1);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Integer> depthMap = transactionDepth.get();

        if (depthMap != null) {
            int currentDepth = depthMap.getOrDefault(key, 0);

            if (currentDepth > 1) {
                depthMap.put(key, currentDepth - 1);
                return null;
            }
            if (currentDepth == 1) {
                depthMap.remove(key);
                if (depthMap.isEmpty()) {
                    transactionDepth.remove();
                }
                Map<DataSource, Connection> resourceMap = resources.get();
                if (resourceMap != null) {
                    Connection connection = resourceMap.remove(key);
                    if (resourceMap.isEmpty()) {
                        resources.remove();
                    }
                    return connection;
                }
            }
            throw new IllegalStateException("Has no resource for key :" + key);
        }

        return null;
    }
}
