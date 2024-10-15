package com.interface21.transaction.support;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager = new TransactionManager(dataSource);
    }

    @Test
    @DisplayName("트랜잭션이 시작 성공: auto-commit이 false로 설정")
    void testBeginTransaction() throws SQLException {
        // given & when
        transactionManager.begin();

        // then
        verify(connection).setAutoCommit(false);
    }

    @Test
    @DisplayName("트랜잭션 커밋 성공")
    void testCommitTransaction() throws SQLException {
        // given
        transactionManager.begin();

        // when
        transactionManager.commit();

        // then
        verify(connection).commit();
    }

    @Test
    @DisplayName("롤백 성공")
    void testRollbackTransaction() throws SQLException {
        // given
        transactionManager.begin();

        // when
        transactionManager.rollback();

        // then
        verify(connection).rollback();
    }

    @Test
    @DisplayName("트랜잭션 내 실행 성공 : 커밋")
    void testExecuteInTransactionSuccess() throws SQLException {
        // given & when
        transactionManager.executeInTransaction(() -> {
        });

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
    }

    @Test
    @DisplayName("트랜잭션 내 실행 실패 : 롤백")
    void testExecuteInTransactionFailure() throws SQLException {
        // given & when
        assertThrows(DataAccessException.class, () -> {
            transactionManager.executeInTransaction(() -> {
                throw new RuntimeException();
            });
        });

        // then
        verify(connection).rollback();
    }
}


