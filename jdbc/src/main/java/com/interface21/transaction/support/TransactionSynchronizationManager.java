package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    // 현재 스레드의 Map에서 조회
    public static Connection getResource(DataSource key) {

        return resources.get().get(key);
    }

    // 현재 스레드의 Map에 등록
    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    // 등록된 연결 제거, Map이 비면 ThreadLocl.remove()
    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();

        Connection connection = resourceMap.remove(key);

        if (resourceMap.isEmpty()) {
            resources.remove();
        }

        return connection;
    }
}
