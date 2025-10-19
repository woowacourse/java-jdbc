package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class Transaction {

    private final DataSource dataSource;

    private Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Transaction init(DataSource dataSource) {
        return new Transaction(dataSource);
    }

    public void begin() {
        try {
            final Connection conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void rollback() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            releaseConnection(connection);
        }
    }

    private void releaseConnection(final Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
