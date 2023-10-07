package org.springframework.jdbc.core;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

public class TransactionManager {
    private static final ThreadLocal<Connection> resources = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

    public static void start() {
        actualTransactionActive.set(Boolean.TRUE);
    }

    public static Connection getConnection(final DataSource dataSource) {
        if (Objects.isNull(resources.get())) {
            resources.set(createConnection(dataSource));
        }

        final var connection = resources.get();
        setAutoCommit(connection);
        return connection;
    }

    private static void setAutoCommit(final Connection connection) {
        process(() -> connection.setAutoCommit(false));
    }

    private static Connection createConnection(final DataSource dataSource) {
        try {
            return requireNonNull(dataSource, "DataSource가 null입니다.").getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commit() {
        process(resources.get()::commit);
    }

    public static void rollback() {
        process(resources.get()::rollback);
    }

    public static void release() {
        final var connection = resources.get();
        resources.remove();
        actualTransactionActive.remove();
        process(connection::close);
    }

    private static void process(final TransactionProcessor processor) {
        try {
            runWithinTransaction(processor);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runWithinTransaction(final TransactionProcessor processor) throws SQLException {
        if (isTransactionActive()) {
            processor.process();
        }
    }

    private static boolean isTransactionActive() {
        final var isActive = actualTransactionActive.get();
        return Objects.nonNull(isActive) && isActive;
    }
}
