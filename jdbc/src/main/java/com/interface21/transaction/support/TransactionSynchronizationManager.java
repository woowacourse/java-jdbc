package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final Map<DataSource, Connection> resources = new ConcurrentHashMap<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return resources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.containsKey(key)) {
            throw new DataAccessException("이미 존재하는 Connection 입니다");
        }
        resources.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        resources.remove(key);
    }
}
