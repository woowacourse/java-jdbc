package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationManager.class);

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        if (getResource(key) != null) {
            throw new IllegalStateException("Already bound resource, key: " + key);
        }

        final Map<DataSource, Connection> map = getOrCreateResourceMap();
        map.put(key, value);
    }

    private static Map<DataSource, Connection> getOrCreateResourceMap() {
        final Map<DataSource, Connection> map = resources.get();
        if (map != null) {
            return map;
        }

        final Map<DataSource, Connection> newMap = new HashMap<>();
        resources.set(newMap);
        return newMap;
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            log.error("Not bound resources map, key: {}", key);
            return null;
        }

        final Connection value = map.remove(key);
        if (value == null) {
            log.error("Not bound connection, key: {}", key);
            return null;
        }

        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }

    public static boolean isConnectionTransactional(final Connection connection, final DataSource dataSource) {
        final Connection boundConnection = getResource(dataSource);
        if (boundConnection == null) {
            return false;
        }

        return connection == boundConnection;
    }
}
