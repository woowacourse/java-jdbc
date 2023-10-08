package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.transaction.exception.TransactionManagerException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    @Nullable
    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> targetResources = resources.get();

        if (isInvalidConnection(targetResources, key)) {
            removeResources(targetResources);

            return null;
        }

        return targetResources.get(key);
    }

    private static boolean isInvalidConnection(
            final Map<DataSource, Connection> targetResources,
            final DataSource key
    ) {
        return targetResources == null || !targetResources.containsKey(key);
    }

    private static void removeResources(final Map<DataSource, Connection> targetResources) {
        if (targetResources == null || targetResources.isEmpty()) {
            resources.remove();
        }
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> targetResources = getTargetResources();

        if (targetResources.containsKey(key)) {
            throw new TransactionManagerException("해당 DataSource의 Connection을 이미 사용하고 있습니다.");
        }

        targetResources.put(key, value);
    }

    private static Map<DataSource, Connection> getTargetResources() {
        final Map<DataSource, Connection> targetResources = resources.get();

        if (targetResources != null) {
            return targetResources;
        }

        final HashMap<DataSource, Connection> newTargetResources = new HashMap<>();

        resources.set(newTargetResources);

        return newTargetResources;
    }

    @Nullable
    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> targetResources = resources.get();

        if (targetResources == null) {
            return null;
        }

        final Connection targetConnection = targetResources.remove(key);

        removeResources(targetResources);

        return targetConnection;
    }
}
