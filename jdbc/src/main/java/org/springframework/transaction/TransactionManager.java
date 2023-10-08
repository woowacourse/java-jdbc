package org.springframework.transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    public void begin() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection.commit();
        this.close();
    }

    public void rollback() throws SQLException {
        connection.rollback();
        this.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        DataSourceUtils.releaseConnection(connection, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}
