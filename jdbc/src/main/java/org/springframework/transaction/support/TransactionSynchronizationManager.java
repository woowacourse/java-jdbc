package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =  ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
    }


    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        final Connection removedConnection = dataSourceConnectionMap.remove(key);

        return removedConnection;
    }
}
