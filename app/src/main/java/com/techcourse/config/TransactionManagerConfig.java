package com.techcourse.config;

import java.sql.Connection;
import java.util.Objects;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.GeneralTransactionManager;
import com.interface21.jdbc.core.TransactionManager;

public class TransactionManagerConfig {

    private static TransactionManager INSTANCE;

    public static TransactionManager getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = new GeneralTransactionManager();
        }
        return INSTANCE;
    }

    public static Connection getCurrentConnection(DataSource dataSource) {
        return getInstance().getCurrentConnection(dataSource);
    }

    public static <T> T executeInTransaction(DataSource dataSource, TransactionCallback<T> callback) {
        TransactionManager instance = getInstance();
        instance.begin(dataSource);
        try {
            T result = callback.execute(getCurrentConnection(dataSource));
            instance.commit(dataSource);
            return result;
        } catch (Exception e) {
            instance.rollback(dataSource);
            throw new DataAccessException(e);
        }
    }

    public static void executeInTransaction(DataSource dataSource, VoidTransactionCallback callback) {
        TransactionManager instance = getInstance();
        instance.begin(dataSource);
        try {
            callback.execute(getCurrentConnection(dataSource));
            instance.commit(dataSource);
        } catch (Exception e) {
            instance.rollback(dataSource);
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws Exception;
    }

    @FunctionalInterface
    public interface VoidTransactionCallback {
        void execute(Connection connection) throws Exception;
    }

    private TransactionManagerConfig() {}
}
