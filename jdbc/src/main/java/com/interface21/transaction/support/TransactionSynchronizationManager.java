package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationManager.class);

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        log.info("get resource: {}", key);
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        log.info("bind resource: {}", key);
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        log.info("unbind resource: {}", key);
        return resources.get().remove(key);
    }
}
