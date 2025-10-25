package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {
    // ThreadLocal을 사용하여 스레드별로 트랜잭션 리소스와 동기화 정보를 관리한다.
    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    // 현재 스레드에 바인딩된 리소스를 조회
    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    // 현재 스레드에 리소스를 바인딩
    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}
