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

    public void executeWithoutResult(final TransactionCallback transactionCallback) {
        try (final Connection connection = DataSourceUtils.getConnection(dataSource)) {
            try {
                connection.setAutoCommit(false);

                transactionCallback.doBizLogic();

                connection.commit();
            } catch (final SQLException | DataAccessException e) {
                connection.rollback();
                throw new DataAccessException(e);
            } finally {
                connection.setAutoCommit(true);
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
