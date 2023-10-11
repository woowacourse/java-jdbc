package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static ConnectionHolder getResource(DataSource key) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, ConnectionHolder value) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static ConnectionHolder unbindResource(DataSource key) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map.isEmpty() || !map.containsKey(key)) { // 새로 추가된 코드: map이 비어있다면, 스레드 로컬에서 Map<DataSource, ConnectionHolder> 객체를 삭제
            return null;
        }
        return map.remove(key);
    }
}
