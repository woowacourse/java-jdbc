package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nullable
    public <T> T doInTransaction(final Supplier<T> supplier) throws DataAccessException {
        T result = null;
        try (final var connection = DataSourceUtils.getConnection(dataSource)) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            connection.setAutoCommit(false);
            result = supplier.get();
            connection.commit();
        } catch (final Exception e) {
            doRollback(e, TransactionSynchronizationManager.getResource(dataSource));
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        return result;
    }

    private void doRollback(final Exception e, final Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException(e);
        } catch (final SQLException ex) {
            throw new DataAccessException("rollback failed by : " + e.getMessage(), ex);
        }
    }
}
