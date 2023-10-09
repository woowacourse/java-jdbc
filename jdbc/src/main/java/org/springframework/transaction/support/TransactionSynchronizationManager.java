package org.springframework.transaction.support;

import java.sql.SQLException;
import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }

}
