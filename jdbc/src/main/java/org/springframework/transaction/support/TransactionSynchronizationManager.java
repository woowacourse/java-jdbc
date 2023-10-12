package org.springframework.transaction.support;

import java.util.HashMap;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    @Nullable
    public static Connection getResource(final DataSource key) {
        return getResources().getOrDefault(key, null);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        getResources().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        return getResources().remove(key);
    }

    private static Map<DataSource, Connection> getResources() {
        return resources.get();
    }
}
