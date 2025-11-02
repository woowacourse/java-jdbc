package com.interface21.transaction.support;

import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Map<DataSource, Boolean>> isActiveTransaction = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
             return null;
        }
        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new ConcurrentHashMap<>();
            resources.set(dataSourceConnectionMap);
        }
        dataSourceConnectionMap.putIfAbsent(key, value); // requires_new 지원안하기
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            throw new IllegalStateException();
        }
        return dataSourceConnectionMap.remove(key);
    }

    public static boolean isActiveTransaction(DataSource key) {
        Map<DataSource, Boolean> dataSourceConnectionMap = isActiveTransaction.get();
        if (dataSourceConnectionMap == null) {
            return false;
        }
        return dataSourceConnectionMap.getOrDefault(key, false);
    }

    public static void activateTransaction(DataSource key) {
        Map<DataSource, Boolean> dataSourceConnectionMap = isActiveTransaction.get();
        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new ConcurrentHashMap<>();
            isActiveTransaction.set(dataSourceConnectionMap);
        }
        dataSourceConnectionMap.put(key, true);
    }

    public static void deactivateTransaction(DataSource key) {
        Map<DataSource, Boolean> dataSourceConnectionMap = isActiveTransaction.get();
        if (dataSourceConnectionMap == null) {
            throw new IllegalStateException();
        }
        dataSourceConnectionMap.put(key, false);

    }
}
