package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final Supplier<T> action) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            return action.get();
        }

        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return executeInTransaction(connection, action);
        } catch (SQLException e) {
            final var dataAccessException = new DataAccessException(e);
            failure = dataAccessException;
            throw dataAccessException;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) == connection) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            DataSourceUtils.releaseConnection(connection, dataSource, failure);
        }
    }

    private <T> T executeInTransaction(final Connection connection, final Supplier<T> action) throws SQLException {
        final var autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final T result;
        try {
            result = action.get();
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollbackAndRestoreAutoCommit(connection, autoCommit, e);
            throw e;
        }

        connection.setAutoCommit(autoCommit);
        return result;
    }

    private void rollbackAndRestoreAutoCommit(final Connection connection, final boolean autoCommit,
                                            final Throwable failure) {
        try {
            connection.rollback();
            // 롤백 실패 후 true로 바꾸면 미완료 작업이 커밋될 수 있으므로 성공한 경우에만 복원한다.
            connection.setAutoCommit(autoCommit);
        } catch (SQLException | RuntimeException | Error e) {
            if (failure != e) {
                failure.addSuppressed(e);
            }
        }
    }
}
