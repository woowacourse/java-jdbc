package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(Consumer<Connection> consumer) {
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            consumer.accept(conn);
            conn.commit();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e);
        }
    }
}
