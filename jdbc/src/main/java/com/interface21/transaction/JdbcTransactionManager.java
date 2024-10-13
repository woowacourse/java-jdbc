package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void doInTransaction(Consumer<Connection> action) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            action.accept(connection);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException("트랜잭션에 실패했습니다.", e);
        } finally {
            closeConnection(connection);
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (Exception e) {
                throw new DataAccessException("롤백에 실패했습니다.", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                throw new DataAccessException("커넥션 닫기에 실패했습니다.", e);
            }
        }
    }
}
