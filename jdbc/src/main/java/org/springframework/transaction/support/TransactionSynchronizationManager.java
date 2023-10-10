package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> transactionEnables =  ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TransactionSynchronizationManager() {}

    private static Map<DataSource, Connection> resource() {
        return resources.get();
    }

    public static Connection getResource(final DataSource key) {
        return resource().get(key);
    }

    public static void bindResource(final DataSource key,
                                    final Connection value) {
        resource().put(key, value);
    }

    public static void unbindResource(final DataSource key) {
        transactionEnables.remove();
        resource().remove(key);
    }

    public static boolean isTransactionEnable() {
        return transactionEnables.get();
    }

    public static void begin() {
        transactionEnables.set(Boolean.TRUE);
    }
}
