package org.springframework.transaction.support;

import static java.lang.ThreadLocal.withInitial;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, SimpleConnectionHolder>> resources = withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    private static Map<DataSource, SimpleConnectionHolder> resource() {
        return resources.get();
    }

    public static SimpleConnectionHolder getResource(final DataSource key) {
        return resource().get(key);
    }

    public static void bindResource(final DataSource key, final SimpleConnectionHolder value) {
        resource().put(key, value);
    }

    public static SimpleConnectionHolder unbindResource(final DataSource key) {
        return resource().remove(key);
    }
}
