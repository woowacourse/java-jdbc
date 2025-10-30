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
        Map<DataSource, Connection> savedResource = getResource();
        return savedResource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> savedResource = getResource();
        savedResource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return removeResource(key);
    }

    private static Map<DataSource, Connection> getResource() {
        Map<DataSource, Connection> savedResource = resources.get();
        if (savedResource == null) {
            Map<DataSource, Connection> newSavedResource = new HashMap<>();
            resources.set(newSavedResource);
            return newSavedResource;
        }
        return savedResource;
    }

    private static Connection removeResource(DataSource key) {
        Map<DataSource, Connection> savedResource = getResource();
        Connection removedResource = savedResource.remove(key);
        if (savedResource.isEmpty()) {
            resources.remove();
        }
        return removedResource;
    }
}
