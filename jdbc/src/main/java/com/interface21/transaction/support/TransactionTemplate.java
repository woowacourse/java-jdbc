package com.interface21.transaction.support;

import com.interface21.dao.SqlExecutionException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable callback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            callback.run();

            connection.commit();
        } catch (Exception e) {
            rollbackAndThrow(connection, e);
        } finally {
            DataSourceUtils.doReleaseConnection(connection, dataSource);
        }
    }

    private void rollbackAndThrow(Connection connection, Exception originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            originalException.addSuppressed(rollbackEx);
        }
        throw new SqlExecutionException(originalException);
    }
}