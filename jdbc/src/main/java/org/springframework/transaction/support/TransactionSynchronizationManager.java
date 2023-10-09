package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static ConnectionHolder getResource(DataSource key) {
        return resource().get(key);
    }

    public static void bindResource(DataSource key, ConnectionHolder value) {
        resource().put(key, value);
    }

    public static ConnectionHolder unbindResource(DataSource key) {
        return resource().remove(key);
    }

    private static Map<DataSource, ConnectionHolder> resource() {
        return resources.get();
    }
}
