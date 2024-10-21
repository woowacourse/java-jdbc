package com.interface21.template;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionCallBack;
import com.interface21.jdbc.manager.TransactionManager;

import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T doTransaction(TransactionCallBack<T> transactionCallBack) {
        TransactionManager transactionManager = new TransactionManager(dataSource);

        try {
            transactionManager.doBegin(dataSource);
            T result = transactionCallBack.doExecute();
            transactionManager.doCommit(dataSource);
            return result;
        } catch (DataAccessException e) {
            transactionManager.doRollback(dataSource);
            throw e;
        } finally {
            transactionManager.doClose(dataSource);
        }
    }

    public void doTransaction(Runnable runnable) {
        TransactionManager transactionManager = new TransactionManager(dataSource);

        try {
            transactionManager.doBegin(dataSource);
            runnable.run();
            transactionManager.doCommit(dataSource);
        } catch (DataAccessException e) {
            transactionManager.doRollback(dataSource);
            throw e;
        } finally {
            transactionManager.doClose(dataSource);
        }
    }
}
