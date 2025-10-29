package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.interface21.dao.DataAccessException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }

        if (!resources.get().containsKey(key)) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (!resources.get().containsKey(key)) {
            throw new DataAccessException();
        }
        return resources.get().remove(key);
    }
}
