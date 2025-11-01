package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =
            ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static boolean hasResource(DataSource key) {
        return getResources().containsKey(key);
    }

    public static Connection getResource(DataSource key) {
        return getResources().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (value == null) {
            throw new IllegalArgumentException("Connection은 null일 수 없습니다.");
        }
        getResources().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getResources().remove(key);
    }

    private static Map<DataSource, Connection> getResources() {
        return resources.get();
    }

    public static void clear() {
        resources.remove();
    }
}
