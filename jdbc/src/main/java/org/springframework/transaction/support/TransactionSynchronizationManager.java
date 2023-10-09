package org.springframework.transaction.support;

import static java.lang.ThreadLocal.withInitial;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> actualTransactionActive = withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getResources().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getResources().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getResources().remove(key);
    }

    private static Map<DataSource, Connection> getResources() {
        return resources.get();
    }

    public static boolean isActualTransactionActive() {
        return actualTransactionActive.get();
    }

    public static void setActualTransactionActive(boolean active) {
        actualTransactionActive.set(active);
    }
}
