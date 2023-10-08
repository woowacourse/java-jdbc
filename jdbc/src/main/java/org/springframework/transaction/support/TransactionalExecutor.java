package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalExecutor {

    private final DataSource dataSource;

    public TransactionalExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(BusinessLogicProcessor businessLogicProcessor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            businessLogicProcessor.process();

            connection.commit();
        } catch (Exception e) {
            rollbackAndThrowException(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollbackAndThrowException(final Connection connection, final Exception e) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
        throw new DataAccessException(e);
    }
}
