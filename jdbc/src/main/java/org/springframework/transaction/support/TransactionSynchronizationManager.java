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
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        dataSourceConnectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        Connection value = dataSourceConnectionMap.remove(key);

        if (dataSourceConnectionMap.isEmpty()) {
            resources.remove();
        }

        return value;
    }

}
