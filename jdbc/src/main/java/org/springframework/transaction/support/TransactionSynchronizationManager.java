package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if(resources.get() == null) {
            resources.set(new HashMap<>());
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() != null) {
            return resources.get().remove(key);
        }
        return null;
    }
}
