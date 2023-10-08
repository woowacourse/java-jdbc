package org.springframework.transaction.support;

import static java.lang.ThreadLocal.withInitial;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    private static Map<DataSource, ConnectionHolder> resource() {
        return resources.get();
    }

    public static ConnectionHolder getResource(final DataSource key) {
        return resource().get(key);
    }

    public static void bindResource(final DataSource key, final ConnectionHolder value) {
        resource().put(key, value);
    }

    public static ConnectionHolder unbindResource(final DataSource key) {
        return resource().remove(key);
    }
}
