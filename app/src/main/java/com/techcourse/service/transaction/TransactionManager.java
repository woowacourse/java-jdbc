package com.techcourse.service.transaction;

import com.interface21.jdbc.DataAccessException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transaction(TransactionExecutor executor) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            executor.execute(connection);

            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
        } finally {
            closeConnection(connection);
        }
    }

    public <T>T transaction(TransactionFunction<T> function) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            T result = function.apply(connection);

            connection.commit();
            return result;
        } catch (SQLException exception) {
            rollback(connection);
        } finally {
            closeConnection(connection);
        }
        return null;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException("롤백 중 예외가 발생했습니다.", exception);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException exception) {
                throw new DataAccessException("커넥션을 닫는 중 예외가 발생했습니다.", exception);
            }
        }
    }
}
