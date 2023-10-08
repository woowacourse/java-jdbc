package org.springframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doTransaction(final TransactionCallback transactionCallback) {
        try {
            doInternalTransaction(transactionCallback);
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e);
        }
    }

    private void doInternalTransaction(final TransactionCallback transactionCallback) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            transactionCallback.execute();

            conn.commit();
        } catch (SQLException | DataAccessException e) {
            conn.rollback();
            log.warn("트랜잭션 내에서 오류가 발생하여 롤백합니다.", e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
