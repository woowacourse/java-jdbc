package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.exception.TransactionAutoCommitException;
import org.springframework.transaction.exception.TransactionCommitException;
import org.springframework.transaction.exception.TransactionRollbackException;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final Supplier<T> supplier) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        setAutoCommit(false, conn);

        try {
            final T value = supplier.get();
            commit(conn);
            return value;
        } catch (final RuntimeException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void setAutoCommit(final boolean autoCommit, final Connection conn) {
        try {
            conn.setAutoCommit(autoCommit);
            System.out.println("conn.getAutoCommit() = " + conn.getAutoCommit());
        } catch (final SQLException e) {
            throw new TransactionAutoCommitException(e);
        }
    }

    private void commit(final Connection conn) {
        try {
            conn.commit();
        } catch (final SQLException e) {
            throw new TransactionCommitException();
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
            System.out.println("conn.getAutoCommit() in rollback = " + conn.getAutoCommit());
        } catch (final SQLException e) {
            throw new TransactionRollbackException(e);
        }
    }
}
