package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessException.RunAndThrowable;
import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private TransactionManager(){

    }

    public static void execute(DataSource dataSource, RunAndThrowable runAndThrowable) {
        bindAndStartTransaction(dataSource);
        try {
            runAndThrowable.run();
            unbindAndCommit(dataSource);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            unbindAndRollback(dataSource);
            throw new DataAccessException(e);
        }
    }

    private static void bindAndStartTransaction(DataSource key) {
        Connection connection = TransactionSynchronizationManager.getResource(key);
        execute(() -> connection.setAutoCommit(false));
    }

    private static void unbindAndCommit(DataSource key) {
        Connection connection = TransactionSynchronizationManager.unbindResource(key);
        execute(connection::commit);
        execute(connection::close);
    }

    private static void execute(RunAndThrowable runAndThrowable) {
        DataAccessException.executeAndConvertException(runAndThrowable);
    }

    private static void unbindAndRollback(DataSource key) {
        Connection connection = TransactionSynchronizationManager.unbindResource(key);
        execute(connection::rollback);
        execute(connection::close);
    }
}
