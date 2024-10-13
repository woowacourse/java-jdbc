package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.function.Consumer;

public class TransactionManager {

    private final Connection connection;

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    public void performTransaction(final Consumer<Connection> operation) {
        try {
            begin();
            operation.accept(connection);
            commit();
        } catch (Exception e) {
            rollback();
            throw new DataAccessException("에러가 발생해 트랜잭션이 롤백되었습니다.", e);
        }
    }

    private void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 시작에 실패했습니다.", e);
        }
    }

    private void commit() {
        try {
            connection.commit();
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 커밋에 실패했습니다.", e);
        }
    }

    private void rollback() {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 롤백에 실패했습니다.", e);
        }
    }
}
