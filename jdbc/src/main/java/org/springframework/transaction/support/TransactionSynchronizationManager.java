package org.springframework.transaction.support;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    // 쓰레드 별 DataSource - Connection Map을 만든다.
    // 어떤 쓰레드의 DataSource마다 하나의 Connection 생성을 보장할 수 있다.
    // 따라서 이 클래스를 사용하면 하나의 쓰레드가 DataSource 당 하나의 Connection만을 사용한다고 보장한다.

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        // 트랜잭션 시작시 이 메서드로 Connection을 저장한다.
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        // 트랜잭션 끝날 때 저장한 Connection을 제거한다.
        return resources.get().remove(key);
    }
}
