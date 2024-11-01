package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TxManager {

    private TxManager() {
    }

    public static <T> T run(Supplier<T> supplier) {
        try {
            return doRun(supplier);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void run(Runnable runnable) {
        run(() -> {
            runnable.run();
            return null;
        });
    }


    private static <T> T doRun(Supplier<T> supplier) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }
}
