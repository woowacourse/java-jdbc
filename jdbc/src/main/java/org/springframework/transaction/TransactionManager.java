package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final ConnectionAction<T> action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return executeAction(action, connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private <T> T executeAction(final ConnectionAction<T> action, final Connection connection) throws SQLException {
        try {
            return commit(action, connection);
        } catch (SQLException e) {
            connection.rollback();
            throw new DataAccessException(e);
        }
    }

    private <T> T commit(final ConnectionAction<T> action, final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        final T result = action.execute();
        connection.commit();

        return result;
    }
}
