package org.springframework.transaction.support;

import static java.lang.ThreadLocal.withInitial;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionManager>> resources = withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static ConnectionManager getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final ConnectionManager value) {
        if (resources.get().containsKey(key)) {
            throw new CannotGetJdbcConnectionException("Connection is already existed with same data source");
        }
        resources.get().put(key, value);
    }

    public static ConnectionManager unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}
