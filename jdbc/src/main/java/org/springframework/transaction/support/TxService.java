package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.exception.CommitFailException;
import org.springframework.transaction.exception.RollbackFailException;

public abstract class TxService {

    private final DataSource dataSource;

    protected TxService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> T executeTransaction(final TransactionExecutor<T> executor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final T queryResult = executor.execute();
            connection.commit();
            return queryResult;
        } catch (final SQLException e) {
            rollback(connection);
            throw new CommitFailException("fail commit!");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackFailException("fail rollback!");
        }
    }
}
