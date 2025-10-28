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
            DataSource dataSource = DataSourceConfig.getInstance();
            INSTANCE = new GeneralTransactionManager(dataSource);
        }
        return INSTANCE;
    }

    public static Connection getCurrentConnection() {
        return getInstance().getCurrentConnection();
    }

    public static Connection getOrCreateConnection() {
        return getInstance().getOrCreateConnection();
    }

    public static <T> T executeInTransaction(TransactionCallback<T> callback) {
        TransactionManager instance = getInstance();
        instance.begin();
        try {
            T result = callback.execute(getCurrentConnection());
            instance.commit();
            return result;
        } catch (Exception e) {
            instance.rollback();
            throw new DataAccessException(e);
        }
    }

    public static void executeInTransaction(VoidTransactionCallback callback) {
        TransactionManager instance = getInstance();
        instance.begin();
        try {
            callback.execute(getCurrentConnection());
            instance.commit();
        } catch (Exception e) {
            instance.rollback();
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
