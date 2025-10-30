package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {
    // ThreadLocal을 사용하여 스레드별로 트랜잭션 리소스와 동기화 정보를 관리한다.
    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    // 현재 스레드에 바인딩된 리소스를 조회
    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSources = resources.get();
        if (dataSources == null) {
            return null;
        }
        return dataSources.get(key);
    }

    // 현재 스레드에 리소스를 바인딩
    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSources = resources.get();
        if (dataSources == null) {
            dataSources = new HashMap<>();
            resources.set(dataSources);
        }

        dataSources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSources = resources.get();
        if (dataSources == null) {
            return null;
        }
        return dataSources.remove(key);
    }
}
