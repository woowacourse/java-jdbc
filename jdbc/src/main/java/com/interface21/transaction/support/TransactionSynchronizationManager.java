package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> resourceMap = getResourceMap();
        return resourceMap.get(key);
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

    private static Map<DataSource, Connection> getResourceMap() {
        final Map<DataSource, Connection> resourceMap = resources.get();
        validateResourceMap(resourceMap);
        return resourceMap;
    }

    private static void validateResourceMap(final Map<DataSource, Connection> resourceMap) {
        if (resourceMap == null) {
            throw new IllegalStateException("No thread-bound resources");
        }
    }
}
