package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    private static Map<DataSource, Connection> currentResource() {
        return resources.get();
    }

    public static Connection getResource(DataSource key) {
        return currentResource().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        currentResource().put(key, value);
    }

    public static void unbindResource(DataSource key) {
        currentResource().remove(key);
    }

    public static boolean hasResource(DataSource dataSource) {
        return currentResource().containsKey(dataSource);
    }
}
