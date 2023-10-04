package org.springframework.transaction.support;

import java.sql.Connection;

public class TransactionContext {

    private static final ThreadLocal<Connection> status = new ThreadLocal<>();

    public static void set(final Connection connection) {
        status.set(connection);
    }

    public static Connection get() {
        return status.get();
    }

    public static void remove() {
        status.remove();
    }

    public static boolean isEmpty() {
        return status.get() == null;
    }
}
