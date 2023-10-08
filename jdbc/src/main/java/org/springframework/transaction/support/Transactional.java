package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.JdbcException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class Transactional {

    private static Transactional instance;
    private final ThreadLocal<Connection> resource;
    private final ThreadLocal<DataSource> dataSource;

    public static synchronized Transactional getInstance() {
        if (instance == null) {
            instance = new Transactional();
        }
        return instance;
    }

    private Transactional() {
        this.resource = new ThreadLocal<>();
        this.dataSource = new ThreadLocal<>();
    }

    public Connection getConnection(final DataSource dataSource) {
        if (resource.get() != null) {
            return resource.get();
        }
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            this.resource.set(connection);
            this.dataSource.set(dataSource);
            return connection;
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("", e);
        }
    }

    public void rollback() {
        try {
            resource.get().rollback();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public void commit() throws SQLException {
        resource.get().commit();
    }

    public void close() {
        try {
            resource.get().close();
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            DataSourceUtils.releaseConnection(resource.get(), dataSource.get());
            resource.remove();
            dataSource.remove();
        }
    }
}
