package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getThreadResource().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getThreadResource().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getThreadResource().remove(key);
    }

    private static Map<DataSource, Connection> getThreadResource() {
        if(resources.get() == null){
            resources.set(new HashMap<>());
        }
        return resources.get();
    }
}
