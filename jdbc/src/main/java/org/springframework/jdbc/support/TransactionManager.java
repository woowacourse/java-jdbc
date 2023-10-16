package org.springframework.jdbc.support;

import javax.sql.DataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    public static void beginTransaction() {
        TransactionSynchronizationManager.beginTransaction();
    }

    public static void commit(final DataSource dataSource) {
        TransactionSynchronizationManager.commit(dataSource);
    }

    public static void rollback() {
        TransactionSynchronizationManager.rollback();
    }
}
