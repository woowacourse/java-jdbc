package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void commit() {
        try {
            Connection connection = getConnection();
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeConnection();
        }
    }

    @Override
    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        DataSourceUtils.releaseConnection(dataSource);
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}
