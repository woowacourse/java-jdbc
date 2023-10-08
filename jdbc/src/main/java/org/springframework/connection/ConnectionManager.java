package org.springframework.connection;

import org.springframework.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection(final boolean autoCommit) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(autoCommit);
            return connection;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(final Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
