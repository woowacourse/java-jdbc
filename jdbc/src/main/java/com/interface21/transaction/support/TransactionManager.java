package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;
    private Connection connection;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException e) {
            rollback();
            throw new DataAccessException("Failed to commit transaction", e);
        }
    }

    public void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void executeInTransaction(Runnable action) {
        begin();
        try {
            action.run();
            commit();
        } catch (Exception e) {
            rollback();
            throw new DataAccessException("Transaction failed", e);
        }
    }
}
