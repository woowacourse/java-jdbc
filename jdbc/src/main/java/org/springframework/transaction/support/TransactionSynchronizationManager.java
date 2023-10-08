package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        initializeResource();
    }

    private static void initializeResource() {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
            log.info("====> TransactionSynchronizationManager.initializeResource()");
        }
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}
