package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.JdbcException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class Transactional {

    private static Transactional instance;
    private static final ThreadLocal<Connection> resource = new ThreadLocal<>();

    public static synchronized Transactional getInstance() {
        if (instance == null) {
            instance = new Transactional();
        }
        return instance;
    }

    public Connection getConnection(final DataSource dataSource) {
        if (resource.get() != null) {
            return resource.get();
        }
        try {
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            resource.set(connection);
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
            resource.remove();
        }
    }
}
