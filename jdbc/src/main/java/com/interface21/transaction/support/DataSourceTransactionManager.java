package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class DataSourceTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    private DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static DataSourceTransactionManager init(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    public void begin() {
        try {
            final Connection conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            releaseConnection();
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    @Override
    public void commit() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection();
        }
    }

    @Override
    public void rollback() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            releaseConnection();
        }
    }

    private void releaseConnection() {
        final Connection connection = TransactionSynchronizationManager.unbindConnection(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
