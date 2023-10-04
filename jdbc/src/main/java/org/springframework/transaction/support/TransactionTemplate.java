package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.ConnectionManager;

public class TransactionTemplate {

    private TransactionTemplate() {
    }

    public static void execute(Runnable runnable) {
        if (TransactionManager.isPropagation()) {
            runnable.run();
            return;
        }
        TransactionManager.begin();
        try {
            runnable.run();
        } catch (Exception e) {
            TransactionManager.setRollback();
            throw e;
        } finally {
            ConnectionManager.releaseConnection();
        }
    }
}
