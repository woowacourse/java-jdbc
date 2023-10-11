package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionCallback transactionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                connection.setAutoCommit(false);
                TransactionSynchronizationManager.setActualTransactionActiveTrue();
                transactionCallback.doInTransaction(connection);
                connection.commit();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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
            log.error(e.getMessage(), e);
            throw new DataAccessException("rollback 에 실패했습니다.");
        }
    }
}
