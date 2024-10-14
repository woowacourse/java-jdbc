package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.interface21.jdbc.exception.ConnectionCloseException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return RESOURCES.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("DataSource와 Connection은 null이여서는 안됩니다.");
        }
        RESOURCES.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Connection removed = RESOURCES.get().remove(key);
        if (removed != null) {
            try {
                if (!removed.isClosed()) {
                    removed.close();
                }
            } catch (final SQLException e) {
                throw new ConnectionCloseException("커넥션 종료에 실패했습니다.", e);
            }
        }
        return removed;
    }

    public static void unload() {
        RESOURCES.remove();
    }
}
