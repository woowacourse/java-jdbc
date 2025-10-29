package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;

/**
 * ThreadLocal을 사용하여 현재 스레드에서 사용 중인 Connection을 저장하고 관리한다. 이를 통해 같은 트랜잭션 내에서 여러 DAO가 동일한 Connection을 공유할 수 있다.
 */
public abstract class TransactionSynchronizationManager {

    /**
     * 현재 스레드의 DataSource별 Connection을 저장하는 ThreadLocal 스레드마다 독립적인 Map을 가지므로 멀티스레드 환경에서 안전하다.
     */
    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    /**
     * 현재 스레드에 바인딩된 Connection을 조회한다.
     */
    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * 현재 스레드에 Connection을 바인딩한다. 트랜잭션 시작 시 호출되어 Connection을 저장한다. 같은 스레드에서 실행되는 모든 DAO는 이 Connection을 공유하게 된다.
     */
    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            // 현재 스레드에 Map이 없으면 새로 생성
            map = new java.util.HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    /**
     * 현재 스레드에서 Connection 바인딩을 해제한다. 트랜잭션 종료 시 호출되어 Connection 바인딩을 제거한다. Map이 비어있으면 ThreadLocal 자체도 제거하여 메모리 누수를
     * 방지한다.
     */
    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        Connection connection = map.remove(key);
        // Map이 비어있으면 ThreadLocal도 제거하여 메모리 누수 방지
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
