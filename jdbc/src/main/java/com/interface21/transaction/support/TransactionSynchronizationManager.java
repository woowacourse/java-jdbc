package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Connection connection = getThreadResource().get(key);
        if(connection == null) {
            throw new NoSuchElementException("cannot find connection for key " + key);
        }
        return connection;
    }

    public static void bindResource(DataSource key, Connection value) {
        getThreadResource().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Connection connection = getThreadResource().remove(key);
        if(connection != null) {
            throw new NoSuchElementException("cannot unbind connection for key " + key);
        }
        return connection;
    }

    private static Map<DataSource, Connection> getThreadResource() {
        if(resources.get() == null){
            resources.set(new HashMap<>());
        }
        return resources.get();
    }
}
