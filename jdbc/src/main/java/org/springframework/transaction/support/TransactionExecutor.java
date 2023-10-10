package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void transactionCommand(Connection connection, Runnable runnable) {
        try {
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static <T> T transactionQuery(Supplier<T> supplier) {
        return supplier.get();
    }

}
