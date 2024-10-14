package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if(Objects.isNull(connections)){
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = new HashMap<>();
        connections.put(key,value);
        resources.set(connections);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}
