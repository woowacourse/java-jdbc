package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static ConnectionHolder getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, ConnectionHolder value) {
        resources.get().put(key, value);
    }

    public static ConnectionHolder unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}
