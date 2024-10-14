package com.interface21.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

class TransactionTest {

    Connection connection;
    Transaction transaction;

    @BeforeEach
    void setup() {
        connection = Mockito.spy(Connection.class);
        transaction = new Transaction(connection);
    }

    @Test
    @DisplayName("트랜잭션 롤백시 커넥션을 커밋한다.")
    void commit() throws SQLException {
        transaction.commit();
        Mockito.verify(connection, Mockito.times(1))
                .commit();
    }

    @Test
    @DisplayName("트랜잭션 롤백시 커넥션을 롤백한다.")
    void rollback() throws SQLException {
        transaction.rollback();
        Mockito.verify(connection, Mockito.times(1))
                .rollback();
    }

    @Test
    @DisplayName("트랜잭션을 시작하면 Auto Commit 을 false 로 변경한다.")
    void begin() throws SQLException {
        transaction.begin();
        Mockito.verify(connection, Mockito.times(1))
                .setAutoCommit(false);
    }

    @Test
    @DisplayName("트랜잭션 종료시 커넥션도 종료한다.")
    void close() throws SQLException {
        transaction.close();
        Mockito.verify(connection, Mockito.times(1))
                .close();
    }
}
