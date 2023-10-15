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
        Map<DataSource, Connection> map = getMap();
        return map.get(key);
    }

    private static Map<DataSource, Connection> getMap() {
        return resources.get();
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        map.remove(key);
    }
}
