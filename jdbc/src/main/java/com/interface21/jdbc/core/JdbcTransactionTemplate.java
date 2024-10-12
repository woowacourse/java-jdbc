package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcTransactionTemplate {

    private final DataSource dataSource;

    public JdbcTransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransactional(TransactionalCallback callback) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            callback.executeAsTransactional(connection);
            connection.commit();
        } catch (Exception e) {
            wrapConnection(connection, Connection::rollback);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            wrapConnection(connection, Connection::close);
        }
    }

    @FunctionalInterface
    interface ConnectionConsumerWrapper {
        void accept(Connection connection) throws SQLException;
    }

    private void wrapConnection(Connection connection, ConnectionConsumerWrapper consumerWrapper) {
        try {
            if (connection != null) {
                consumerWrapper.accept(connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
