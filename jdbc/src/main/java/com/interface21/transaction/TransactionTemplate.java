package com.interface21.transaction;

import javax.sql.DataSource;

public class TransactionTemplate {

    private final TransactionManager txManager;

    public TransactionTemplate(final DataSource dataSource) {
        this.txManager = new TransactionManager(dataSource);
    }

    public void execute(final BusinessTaskWithoutResult task) {
        txManager.begin();
        try {
            task.runBusinessLogic();
            txManager.commit();
        } catch (Exception e) {
            System.out.println("롤백");
            txManager.rollback();
            throw e;
        }
    }

    public <T> T executeWithResult(final BusinessTaskWithResult<T> task) {
        txManager.begin();
        try {
            final T result = task.runBusinessLogic();
            txManager.commit();
            return result;
        } catch (Exception e) {
            System.out.println("롤백");
            txManager.rollback();
            throw e;
        }
    }
}
