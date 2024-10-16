package com.interface21.transaction.util;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;

public class SQLExceptionUtil {

    public static void handleSQLException(SQLExceptionRunnable runnable) {
        try {
            runnable.run();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    public interface SQLExceptionRunnable {
        void run() throws SQLException;
    }
}
