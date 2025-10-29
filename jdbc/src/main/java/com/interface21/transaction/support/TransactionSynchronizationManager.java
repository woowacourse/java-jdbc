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
        Map<DataSource, Connection> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map.containsKey(key)) {
            throw new IllegalStateException("이미 커넥션이 존재합니다.");
        }
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (!isBound(key)) {
            throw new IllegalStateException("기존에 바인딩 된 연결이 존재하지 않습니다.");
        }
        Map<DataSource, Connection> map = resources.get();
        Connection removed = map.remove(key);
        return removed;
    }

    public static boolean isBound(DataSource key){
        Map<DataSource, Connection> map = resources.get();
        return map.containsKey(key);
    }
}
