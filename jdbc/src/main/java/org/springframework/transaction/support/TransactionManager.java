package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        try {
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            final SimpleConnectionHolder connectionHolder = new SimpleConnectionHolder(connection);
            connectionHolder.setTransactionActive(true);
            TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void execute(final ThrowingConsumer<Connection, SQLException> consumer) {
        final SimpleConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        final Connection connection = connectionHolder.getConnection();
        try {
            consumer.accept(connection);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        execute(Connection::commit);
    }

    public void rollback() {
        execute(Connection::rollback);
    }

    public void close() {
        execute(Connection::close);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
