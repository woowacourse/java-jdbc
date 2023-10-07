package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.ConnectionManager;

public class TransactionTemplate {

    private static final ThreadLocal<Boolean> propagations = new ThreadLocal<>();

    private TransactionTemplate() {
    }

    public static void execute(Runnable runnable) {
        if (isPropagation()) {
            runnable.run();
            return;
        }
        propagations.set(Boolean.TRUE);
        TransactionManager.begin();
        try {
            runnable.run();
        } catch (Exception e) {
            TransactionManager.setRollback();
            throw e;
        } finally {
            ConnectionManager.releaseConnection();
            propagations.remove();
        }
    }

    private static boolean isPropagation() {
        Boolean propagation = propagations.get();
        if (propagation == null) {
            return false;
        }
        return propagation;
    }
}
