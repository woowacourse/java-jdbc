package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void injectTransaction(Consumer<Connection> action) {
        try (Connection conn = dataSource.getConnection()) {
            runTransactionWithAtomicity(action, conn);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private void runTransactionWithAtomicity(Consumer<Connection> action, Connection conn) {
        try {
            conn.setAutoCommit(false);
            action.accept(conn);
            conn.commit();
        } catch (SQLException | DataAccessException e) {
            tryRollBack(conn);
            throw new DataAccessException("트랜잭션 수행 중 문제가 발생하여 롤백하였습니다.", e);
        }
    }

    private void tryRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("롤백 작업 수행 중 문제가 발생하였습니다.", e);
        }
    }
}
