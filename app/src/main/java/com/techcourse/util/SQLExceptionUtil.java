package com.techcourse.util;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;

public class SQLExceptionUtil {

    public static <T> T handleSQLException(SQLExceptionSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void handleSQLException(SQLExceptionRunnable runnable) {
        try {
            runnable.run();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    public interface SQLExceptionSupplier<T> {
        T get() throws SQLException;
    }

    @FunctionalInterface
    public interface SQLExceptionRunnable {
        void run() throws SQLException;
    }
}
