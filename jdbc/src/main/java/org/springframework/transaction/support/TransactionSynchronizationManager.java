package org.springframework.transaction.support;

import static java.util.Objects.isNull;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        return resource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resource = new HashMap<>();
        resource.put(key, value);
        resources.set(resource);
    }

    public static Connection unbindResource(DataSource key) {
        Connection resource = getResource(key);
        if (isNull(resource)) {
            throw new IllegalArgumentException("There is no resource");
        }
        
        resources.remove();
        return resource;
    }
}
