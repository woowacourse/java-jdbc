package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (getResource(key) != null) {
            throw new DataAccessException("Transaction already started for this datasource");
        }
        resources.get().put(key, value);
    }

    public static void unbindResource(DataSource key) {
        resources.get().remove(key);
    }

    public static boolean doesNotManage(DataSource dataSource) {
        return getResource(dataSource) == null;
    }
}
