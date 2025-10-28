package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static boolean hasConnection(DataSource dataSource) {
        return resources.get().containsKey(dataSource);
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> resourceMap = resources.get();
        if (!resourceMap.containsKey(key)) {
            throw new IllegalStateException("unbind할 리소스가 존재하지 않습니다.");
        }
        return resourceMap.remove(key);
    }

    public static void clear() {
        resources.remove();
    }
}
