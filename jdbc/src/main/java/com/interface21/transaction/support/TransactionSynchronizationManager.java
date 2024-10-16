package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static boolean isActive(DataSource key) {
        Map<DataSource, Connection> localResources = resources.get();
        return localResources.containsKey(key);
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> localResources = resources.get();
        return localResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> localResources = resources.get();
        if (localResources.containsKey(key)) {
            throw new DataAccessException("DataSource " + key + " 에 대한 Connection이 이미 존재합니다.");
        }
        localResources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> localResources = resources.get();
        return localResources.remove(key);
    }
}
