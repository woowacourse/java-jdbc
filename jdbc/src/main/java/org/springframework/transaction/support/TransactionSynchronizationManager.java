package org.springframework.transaction.support;

import static java.lang.ThreadLocal.withInitial;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.exception.ConnectionBindingException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> actualTransactionActive = withInitial(() -> false);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection == null) {
            return null;
        }
        return dataSourceConnection.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();

        if (dataSourceConnection.containsKey(key)) {
            throw new ConnectionBindingException("커넥션이 이미 바인딩되어 있습니다.");
        }
        dataSourceConnection.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection == null || !dataSourceConnection.containsKey(key)) {
            throw new ConnectionBindingException("바인딩 된 커넥션이 존재하지 않습니다.");
        }
        return dataSourceConnection.remove(key);
    }

    public static boolean isActualTransactionActive() {
        return actualTransactionActive.get();
    }

    public static void setActualTransactionActive(boolean active) {
        actualTransactionActive.set(active);
    }
}
