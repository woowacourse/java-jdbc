package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isActive = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource dataSource) throws SQLException {
        if (resources.get().containsKey(dataSource)) {
            return resources.get().get(dataSource);
        }
        final Connection connection = dataSource.getConnection();
        bindResource(dataSource, connection);
        return connection;
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return null;
    }
}
