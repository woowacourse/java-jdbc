package com.interface21.transaction.support;

import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
    private static final ThreadMap<DataSource, Connection> resources = new ThreadMap<>();
    private static final ThreadLocal<Boolean> transactionActive = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        logger.info("dataSource : {}", key);
        logger.info("thread : {}", Thread.currentThread().getName());
        return resources.getMap(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.remove(key);
    }

    public static boolean isTransactionActive() {
        return transactionActive.get();
    }

    public static void setTransactionActive(boolean active) {
        transactionActive.set(active);
    }
}
