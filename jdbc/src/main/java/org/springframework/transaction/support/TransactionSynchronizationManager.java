package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =  ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        Connection oldConnection = dataSourceConnectionMap.put(key, value);

        if (oldConnection != null) {
            throw new IllegalStateException(
                    "Already value [" + oldConnection + "] for key [" + value + "] bound to context");
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        Connection removedConnection = dataSourceConnectionMap.remove(key);

        if (removedConnection == null) {
            throw new IllegalStateException("No value for key [" + key + "] bound to context");
        }

        return removedConnection;
    }
}
