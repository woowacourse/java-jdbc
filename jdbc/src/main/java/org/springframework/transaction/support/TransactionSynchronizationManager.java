package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            try {
                Map<DataSource, Connection> threadResource = new HashMap<>();
                resources.set(threadResource);
            } catch (Exception e) {
                throw new DataAccessException("cannot get Connection", e);
            }
        }

        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        try {
            Map<DataSource, Connection> resource = resources.get();
            Connection connection = resource.get(key);
            resources.remove();
            return connection;
        } catch (Exception e) {
            throw new DataAccessException("resource unbind failed", e);
        }
    }
}
