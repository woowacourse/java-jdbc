package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
    }

    public static Connection unbindResource(DataSource key) {
        return null;
    }
}
