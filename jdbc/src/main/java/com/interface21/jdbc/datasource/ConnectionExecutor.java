package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.util.function.Supplier;

public class ConnectionExecutor {

    private ConnectionExecutor() {

    }

    public static void executeTransactional(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            DataSourceUtils.rollbackAllConnections();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseAllConnections();
        }
    }

    public static void execute(Runnable runnable) {
        runnable.run();
        DataSourceUtils.releaseAllConnections();
    }

    public static <T> T supply(Supplier<T> supplier) {
        T result = supplier.get();
        DataSourceUtils.releaseAllConnections();
        return result;
    }
}
