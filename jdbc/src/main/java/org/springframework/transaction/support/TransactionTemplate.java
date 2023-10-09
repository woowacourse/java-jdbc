package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionCallback transactionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setActualTransactionActiveTrue();
            transactionCallback.doInTransaction(connection);
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            rollback(connection);
            throw new DataAccessException("transaction 설정에 오류가 발생했습니다.");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("rollback 에 실패했습니다.");
        }
    }
}
