package com.interface21.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    public void execute(final TransactionCallback callback, final DataSource dataSource) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            callback.execute();

            connection.commit();
        } catch (Exception e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Exception e, Connection connectionToRollback) {
        if (connectionToRollback != null) {
            try {
                connectionToRollback.rollback();
                log.error("Transaction rolled back due to: ", e);
            } catch (SQLException rollbackException) {
                log.error("Failed to rollback transaction", rollbackException);
                e.addSuppressed(rollbackException);
            }
        }
    }
}
