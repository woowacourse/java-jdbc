package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Connection prevConnection = resources.get().put(key, value);

        if (prevConnection != null) {
            throw new IllegalStateException(
                    "Already value [" + prevConnection + "] for key [" + key + "] bound to thread"
            );
        }
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get().get(key) == null) {
            throw new IllegalStateException("No value for key [" + key + "] bound to thread");
        }

        return resources.get().remove(key);
    }
}
