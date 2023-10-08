package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> transactionEnables = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            resource = new HashMap<>();
        }
        return resource.get(key);
    }

    public static void bindResource(final DataSource key,
                                    final Connection value) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            resource = new HashMap<>();
            resources.set(resource);
        }
        resource.put(key, value);
    }

    public static void unbindResource(final DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            throw new IllegalStateException("resources is not initialize!");
        }
        transactionEnables.remove();
        resource.remove(key);
        if (resource.isEmpty()) {
            resources.remove();
        }
    }

    public static boolean isTransactionEnable() {
        Boolean transactionEnable = transactionEnables.get();
        if (transactionEnable == null) {
            return false;
        }
        return transactionEnable;
    }

    public static void begin() {
        transactionEnables.set(Boolean.TRUE);
    }
}
