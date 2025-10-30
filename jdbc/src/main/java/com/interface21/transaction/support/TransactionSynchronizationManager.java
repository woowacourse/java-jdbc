package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

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
        if (dataSourceConnectionMap.containsKey(key)) {
            throw new DataAccessException("이미 바인딩되어있는 리소스 입니다.");
        }
        dataSourceConnectionMap.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (!dataSourceConnectionMap.containsKey(key)) {
            throw new DataAccessException("존재하지 않는 key입니다.");
        }
        dataSourceConnectionMap.remove(key);

        if (dataSourceConnectionMap.isEmpty()) {
            resources.remove();
        }
    }

    public static boolean isBound(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        return dataSourceConnectionMap.containsKey(key);
    }

    public static void unbindResourceIfBound(DataSource key) {
        if (isBound(key)) {
            unbindResource(key);
        }
    }
}
