package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

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
        if (resources.get().containsKey(key)) {
            throw new DataAccessException("이미 존재하는 Connection 입니다");
        }
        resources.get().put(key, value);
    }

    public static void unbindResource(DataSource key) {
        resources.get().remove(key);
    }
}
