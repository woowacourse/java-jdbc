package org.springframework.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<Map<Connection, Boolean>> activeTransactions = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        return map.remove(key);
    }

    public static boolean isActiveTransaction(Connection connection) {
        return activeTransactions.get().getOrDefault(connection, false);
    }

    public static void setActiveTransaction(Connection connection, boolean status) {
        activeTransactions.get().put(connection, status);
    }
}
