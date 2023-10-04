package org.springframework.transaction.support;

import java.util.function.Supplier;
import org.springframework.jdbc.datasource.ConnectionManager;

public class TransactionTemplate {

    private TransactionTemplate() {
    }

    public static void execute(Runnable runnable) {
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

    public static <T> T execute(Supplier<T> supplier) {
        TransactionManager.begin();
        try {
            return supplier.get();
        } catch (Exception e) {
            TransactionManager.setRollback();
            throw e;
        } finally {
            ConnectionManager.releaseConnection();
        }
    }
}
