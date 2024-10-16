package com.interface21.transaction.support;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Object>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isTransactionActive = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Object getResource(DataSource key) {
        Map<DataSource, Object> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Object value) {
        Map<DataSource, Object> map = resources.get();
        Object oldValue = map.put(key, value);
        if (oldValue != null) {
            throw new IllegalStateException("Already value [ %s ] for key [ %s ] bound to thread [ %s]"
                    .formatted(oldValue, key, Thread.currentThread().getName()));
        }
    }

    public static Object unbindResource(DataSource key) {
        Map<DataSource, Object> map = resources.get();
        Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }

    public static void initTransactionActive() {
        if (isTransactionActive()) {
            throw new IllegalStateException("Cannot activate transaction - already active");
        }
        isTransactionActive.set(Boolean.TRUE);

    }

    public static boolean isTransactionActive() {
        return isTransactionActive.get() != null;
    }

    public static void clear() {
        isTransactionActive.remove();
    }
}
