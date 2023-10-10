package org.springframework.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;
    private final Connection connection;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.connection = DataSourceUtils.getConnection(dataSource);
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
            this.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
            this.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
