package org.springframework.transaction.support;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = getResources();
        return connections.get(key);
    }

    private static Map<DataSource, Connection> getResources() {
        if (isNull(resources.get())) {
            resources.set(new HashMap<>());
        }

        return resources.get();
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> resources = getResources();

        if (resources.containsKey(key)) {
            throw new IllegalStateException("이미 커넥션이 존재합니다.");
        }
        resources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> resources = getResources();
        final Connection connection = resources.get(key);

        if (isNull(connection)) {
            throw new IllegalStateException("커넥션이 존재하지 않습니다.");
        }
        resources.remove(key);

        if (resources.isEmpty()) {
            TransactionSynchronizationManager.resources.remove();
        }

        return connection;
    }
}
