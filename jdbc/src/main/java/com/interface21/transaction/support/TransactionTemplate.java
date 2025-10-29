package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) throws DataAccessException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            final T result = callback.doInTransaction();
            connection.commit();
            return result;
        } catch (Exception ex) {
            rollback(connection);
            throw new DataAccessException(ex);
        } finally {
            cleanup(connection);
        }
    }

    private void rollback(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("rollback fail", rollbackEx);
            }
        }
    }

    private void cleanup(final Connection connection) {
        if (connection != null) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
