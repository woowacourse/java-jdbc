package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSynchronizationManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
    private static final ThreadMap<DataSource, Connection> resources = new ThreadMap<>();
    private static final ThreadConnections threadConnections = new ThreadConnections();

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
        return threadConnections.isTransactionActive();
    }

    public static void pushConnection(Connection connection) {
        threadConnections.pushConnection(connection);
    }

    public static Connection popConnection() {
        return threadConnections.popConnection();
    }

    public static long getTransactionDepth() {
        return threadConnections.size();
    }
}
