package org.springframework.transaction;

import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void execute(final TransactionManager transactionManager, final Runnable runnable) {
        try {
            transactionManager.begin();
            runnable.run();
            transactionManager.commit();
        } catch (Exception e) {
            try {
                transactionManager.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        }
    }
}
