package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        if (map.containsKey(key)) {
            throw new IllegalStateException("해당 DataSource에 대한 리소스가 이미 현재 스레드에 바인딩되어 있습니다.");
        }
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("현재 스레드에는 바인딩된 리소스가 없습니다.");
        }
        Connection removed = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        if (removed == null) {
            throw new IllegalStateException("현재 스레드에서 지정된 DataSource에 대한 리소스를 찾을 수 없습니다.");
        }
        return removed;
    }
}
