package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =
            ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<DataSource, Integer>> usageCounts =
            ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Connection connection = resources.get().get(key);
        if (connection == null) {
            return null;
        }

        Map<DataSource, Integer> countMap = usageCounts.get();
        int current = countMap.getOrDefault(key, 0);
        countMap.put(key, current + 1);
        return connection;
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
        usageCounts.get().put(key, 1);
    }

    public static Connection unbindResource(DataSource key, Connection connection) throws SQLException {
        Map<DataSource, Integer> countMap = usageCounts.get();
        Integer current = countMap.get(key);

        if (current == null) {
            return null;
        }

        if (current <= 1) {
            countMap.remove(key);
            Connection removed = resources.get().remove(key);
            if (removed != null) {
                removed.close();
            }
            return removed;
        }

        countMap.put(key, current - 1);
        return resources.get().get(key);
    }
}
