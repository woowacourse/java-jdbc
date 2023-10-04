package org.springframework.transaction.support;

import java.util.function.Supplier;
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
        return propagation != null;
    }

    public static <T> T execute(Supplier<T> supplier) {
        if (isPropagation()) {
            return supplier.get();
        }
        propagations.set(Boolean.TRUE);
        TransactionManager.begin();
        try {
            return supplier.get();
        } catch (Exception e) {
            TransactionManager.setRollback();
            throw e;
        } finally {
            ConnectionManager.releaseConnection();
            propagations.remove();
        }
    }
}
