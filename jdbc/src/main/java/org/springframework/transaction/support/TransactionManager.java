package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeWithReadOnlyTransaction(final Function<Connection, T> function) {
        try (final Connection conn = dataSource.getConnection()) {
            conn.setReadOnly(true);
            conn.setAutoCommit(false);
            final T result = function.apply(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void executeWithTransaction(final Consumer<Connection> consumer) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            consumer.accept(conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                rollback(conn, e);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void rollback(final Connection conn, final SQLException e) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(ex);
        }
    }
}
