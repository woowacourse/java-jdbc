package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static ConnectionHolder getResource(DataSource key) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        ConnectionHolder connectionHolder = new ConnectionHolder(value);
        connectionHolder.setTransactionActive(true);
        map.put(key, connectionHolder);
    }

    public static ConnectionHolder unbindResource(DataSource key) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        return map.remove(key);
    }
}
