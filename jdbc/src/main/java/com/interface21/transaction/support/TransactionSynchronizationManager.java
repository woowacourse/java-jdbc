package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return getResourceMap().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> resourceMap = getResourceMap();
        if (resourceMap.containsKey(key)) {
            throw new IllegalStateException("Already a resource for key: " + key);
        }
        resourceMap.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> resourceMap = getResourceMap();
        if (!resourceMap.containsKey(key)) {
            throw new IllegalStateException("No resource found for key: " + key);
        }
        return resourceMap.remove(key);
    }

    public static void clear() {
        resources.remove();
    }

    private static Map<DataSource, Connection> getResourceMap() {
        return resources.get();
    }
}
