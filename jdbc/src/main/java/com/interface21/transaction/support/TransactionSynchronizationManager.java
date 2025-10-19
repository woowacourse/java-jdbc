package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        return connectionMap.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = resources.get();
        connectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (!connectionMap.containsKey(key)) {
            return null;
        }
        Connection removed = connectionMap.remove(key);
        if (connectionMap.isEmpty()) {
            resources.remove();
        }
        return removed;
    }

    // 현재는 등록한 DataSource가 하나만 있으므로 unbindResource를 호출하면 ThreadLocal의 자원을 정리한다.
    // 하지만 만약 DataSource가 여러 개라면 unbindResource를 각 DataSoruce마다 하지 않는다면 ThreadLocal 누수 위험성이 존재하게된다.
    // 이를 방지하기 위해 전체 자원 정리 메서드를 만듬. 현재는 쓰이지 않음.
    public static void clear() {
        resources.remove();
    }
}
