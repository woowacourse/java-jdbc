package org.springframework.transaction.support;

import java.util.HashMap;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import org.springframework.jdbc.ConnectionHolder;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    @Nullable
    public static ConnectionHolder getResource(final DataSource key) {
        return getResources().getOrDefault(key, null);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, ConnectionHolder> resources = getResources();
        if(resources.containsKey(key)){
            throw new IllegalStateException();
        }
        resources.put(key, new ConnectionHolder(value));
    }

    public static ConnectionHolder unbindResource(final DataSource key) {
        return getResources().remove(key);
    }

    private static Map<DataSource, ConnectionHolder> getResources() {
        return resources.get();
    }
}
