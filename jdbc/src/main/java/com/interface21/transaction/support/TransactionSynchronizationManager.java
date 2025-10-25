package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    public static boolean hasResource(final DataSource dataSource) {
        return resources.get().containsKey(dataSource);
    }

    public static Connection getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        try {
            value.setAutoCommit(false);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        resources.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final var connection = resources.get().remove(key);

        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}
