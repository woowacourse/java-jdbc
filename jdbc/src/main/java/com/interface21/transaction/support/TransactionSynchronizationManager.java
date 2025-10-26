package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * 트랜잭션 동기화를 지원하기 위한 클래스.
 * 스레드마다 독립적으로 트랜잭션을 관리한다.
 *
 * 인스턴스화할 필요도, 상속할 필요도 없으므로 abstract + private 생성자를 사용한다.
 */
public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    /**
     * 현재 스레드의 트랜잭션 컨텍스트에서 특정 DataSource에 바인딩되어 있는 Connection을 반환한다.
     *
     * @param datasource 조회할 Datasource
     * @return 바인딩된 Connection, 없을 시 null
     */
    public static Connection getResource(DataSource datasource) {
        Map<DataSource, Connection> databaseConnections = resources.get();
        if (databaseConnections == null) {
            return null;
        }
        return databaseConnections.get(datasource);
    }

    /**
     * 현재 스레드의 트랜잭션 컨텍스트에 (Datasource, Connection)을 바인딩한다.
     * 이미 해당 DataSource에 Connection이 바인딩되어 있으면 예외를 던진다.
     *
     * @param datasource 바인딩하려는 DataSource
     * @param connection 바인딩하려는 Connection
     */
    public static void bindResource(DataSource datasource, Connection connection) {
        Map<DataSource, Connection> databaseConnections = resources.get();
        if (databaseConnections == null) {
            databaseConnections = new HashMap<>();
            resources.set(databaseConnections);
        }
        if (databaseConnections.containsKey(datasource)) {
            throw new IllegalStateException("이미 Connection이 바인딩되어 있는 DataSource입니다.");
        }
        databaseConnections.put(datasource, connection);
    }

    /**
     * 현재 스레드의 트랜잭션 컨텍스트에서 DataSource 바인딩을 제거한다.
     * 트랜잭션 컨텍스트에 아무 DataSource도 남지 않으면 ThreadLocal을 정리한다.
     *
     * @param datasource Connection 바인딩을 제거하려는 DataSource
     * @return 제거된 Connection, 없을 시 null
     */
    public static Connection unbindResource(DataSource datasource) {
        Map<DataSource, Connection> databaseConnections = resources.get();
        if (databaseConnections == null) {
            return null;
        }
        Connection removedConnection = databaseConnections.remove(datasource);
        if (databaseConnections.isEmpty()) {
            resources.remove();
        }
        return removedConnection;
    }
}
