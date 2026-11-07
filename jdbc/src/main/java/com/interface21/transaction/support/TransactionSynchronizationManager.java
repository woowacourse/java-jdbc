package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/** 트랜잭션에 사용할 연결을 스레드와 데이터소스별로 보관한다. */
public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            return null;
        }
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.containsKey(key)) {
            throw new IllegalStateException("이미 트랜잭션 연결이 바인딩된 데이터소스입니다.");
        }
        connections.put(key, value);
    }

    /** 마지막 연결을 해제하면 저장소까지 비워 재사용되는 스레드에 빈 Map이 남지 않게 한다. */
    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null || !connections.containsKey(key)) {
            throw new IllegalStateException("트랜잭션 연결이 바인딩되지 않은 데이터소스입니다.");
        }
        final Connection connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
