package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get().containsKey(key)) {
            throw new IllegalStateException("Already value [" + resources.get().get(key) + "] for key [" + key + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (!resources.get().containsKey(key)) {
            throw new IllegalStateException("No value for key [" + key + "] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        return resources.get().remove(key);
    }
}
