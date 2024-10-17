package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalTemplate {

    private final DataSource dataSource;

    public TransactionalTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionCallback transactionCallback) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource,connection);
            performTransaction(new Transaction(connection), transactionCallback);
        } catch (final SQLException e) {
            throw new TransactionalException("트랜잭션 작업중 예외가 발생했습니다.", e);
        }
    }

    private void performTransaction(final Transaction transaction, final TransactionCallback transactionCallback) throws SQLException {
        try {
            transaction.begin();
            transactionCallback.execute(transaction);
            transaction.commit();
        } catch (final SQLException e) {
            transaction.rollback();
            throw new TransactionalException("트랜잭션 작업중 예외가 발생했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(transaction.getConnection(), dataSource);
        }
    }
}
