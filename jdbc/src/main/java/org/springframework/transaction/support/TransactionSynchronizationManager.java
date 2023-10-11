package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Connection connection = resources.get().put(key, value);
        if (!Objects.isNull(connection)) {
            throw new IllegalStateException(
                    "Already value [" + connection + "] for key [" + key + "] bound to thread");
        }

    }

    public static Connection unbindResource(DataSource key) {
        final Connection connection = getResource(key);
        if (Objects.isNull(connection)) {
            throw new IllegalStateException("No value for key [" + key + "] bound to thread");
        }
        return resources.get().remove(key);
    }
}
