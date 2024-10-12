package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        logger.info("dataSource : {}", key);
        logger.info("thread : {}", Thread.currentThread().getName());
        return getThreadResource().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getThreadResource().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getThreadResource().remove(key);
    }

    private static Map<DataSource, Connection> getThreadResource() {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        return resources.get();
    }
}
