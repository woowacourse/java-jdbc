package com.interface21.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class TransactionManager {

    private static final Logger log = Logger.getLogger(TransactionManager.class.getName());

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeTransactionOf(TransactionalFunction callback) {
        Connection connection = null;
        boolean shouldThrow = false;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            callback.execute(connection);
            connection.commit();
        } catch (Exception e) {
            gracefulShutdown(connection, Connection::rollback);
            shouldThrow = true;
        } finally {
            gracefulShutdown(connection, Connection::close);
        }
        if (shouldThrow) {
            throw new DataAccessException("트랜잭션 실행 중 문제가 발생했습니다. 트랜잭션은 롤백됩니다.");
        }
    }

    private void gracefulShutdown(Connection connection, ThrowingConsumer<Connection> connectionConsumer) {
        try {
            connectionConsumer.accept(connection);
        } catch (NullPointerException e) {
            log.warning("Connection을 찾을 수 없습니다.");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
