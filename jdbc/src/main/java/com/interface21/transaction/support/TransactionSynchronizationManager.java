package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();

        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();

        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections.containsKey(key)) {
            Connection removedConnection = connections.remove(key);
            clearUp(connections);
            return removedConnection;
        }
        throw new NoSuchElementException("존재하지 않는 연결입니다.");
    }

    private static void clearUp(Map<DataSource, Connection> connections) {
        if (connections.isEmpty()) {
            resources.remove();
        }
    }
}
