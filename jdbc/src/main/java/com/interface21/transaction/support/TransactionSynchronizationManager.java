package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
    private static final ThreadMap<DataSource, Connection> resources = new ThreadMap<>();
    private static final ThreadLocal<AtomicLong> transactionDepth = ThreadLocal.withInitial(AtomicLong::new);

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
        return getTransactionDepth() > 0;
    }

    public static void increaseDepth() {
        transactionDepth.get().incrementAndGet();
    }

    public static void decreaseDepth() {
        transactionDepth.get().decrementAndGet();
    }

    public static long getTransactionDepth() {
        return transactionDepth.get().get();
    }
}
