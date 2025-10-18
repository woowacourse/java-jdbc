package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static boolean hasConnection(DataSource dataSource) {
        if (resources.get() == null) {
            return false;
        }
        return resources.get().containsKey(dataSource);
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            throw new IllegalStateException("unbind할 리소스가 존재하지 않습니다.");
        }
        return resources.get().remove(key);
    }
}
