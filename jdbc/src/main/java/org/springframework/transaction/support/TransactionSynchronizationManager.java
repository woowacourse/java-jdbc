package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getResources().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getResources().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionByDataSource = getResources();
        Connection connection = connectionByDataSource.remove(key);
        if (connectionByDataSource.isEmpty()) {
            resources.remove();
        }
        return connection;
    }

    private static Map<DataSource, Connection> getResources() {
        return resources.get();
    }
}
