package com.interface21.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class TransactionManager {

    private static final Logger log = Logger.getLogger(TransactionManager.class.getName());

    private TransactionManager() {
    }

    public static void executeTransactionOf(TransactionalFunction callback, DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean shouldThrow = false;
        try {
            connection.setAutoCommit(false);
            callback.execute(connection);
            connection.commit();
        } catch (Exception e) {
            gracefulShutdown(connection, Connection::rollback);
            shouldThrow = true;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        if (shouldThrow) {
            throw new DataAccessException("트랜잭션 실행 중 문제가 발생했습니다. 트랜잭션은 롤백됩니다.");
        }
    }

    private static void gracefulShutdown(Connection connection, ThrowingConsumer<Connection> connectionConsumer) {
        try {
            connectionConsumer.accept(connection);
        } catch (NullPointerException e) {
            log.warning("Connection을 찾을 수 없습니다.");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {

        void accept(T connection) throws SQLException;
    }
}
