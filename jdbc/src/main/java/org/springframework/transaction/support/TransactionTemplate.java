package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        begin(connection);
        try {
            runnable.run();

            commit(connection);
        } catch (Exception e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void begin(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
