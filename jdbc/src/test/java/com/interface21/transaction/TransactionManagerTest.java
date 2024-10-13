package com.interface21.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class TransactionManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager = new TransactionManager(dataSource);
    }

    @DisplayName("트랜잭션 실행 성공 테스트")
    @Nested
    public class SuccessTest {

        @DisplayName("반환 타입이 있는 트랜잭션 실행 메서드가 성공적으로 종료되면 커밋된다.")
        @Test
        void executeSuccessWithReturnType() {
            String result = transactionManager.runInTransaction(() -> "kaki");

            assertAll(
                    () -> assertThat(result).isEqualTo("kaki"),
                    () -> verify(connection).setAutoCommit(false),
                    () -> verify(connection).commit(),
                    () -> verify(connection).setAutoCommit(true),
                    () -> verify(connection, never()).rollback()
            );
        }

        @DisplayName("반환 타입이 없는 트랜잭션 실행 메서드가 성공적으로 종료되면 커밋된다.")
        @Test
        void executeSuccessWithVoid() {
            boolean[] consumerResults = {false, false};

            transactionManager.runInTransaction(() -> consumerResults[0] = true);

            assertAll(
                    () -> assertThat(consumerResults).containsExactly(true, false),
                    () -> verify(connection).setAutoCommit(false),
                    () -> verify(connection).commit(),
                    () -> verify(connection).setAutoCommit(true),
                    () -> verify(connection, never()).rollback()
            );
        }
    }

    @DisplayName("트랜잭션 실행 실패 테스트")
    @Nested
    public class FailTest {

        @DisplayName("반환 타입이 있는 트랜잭션 실행 메서드에서 예외가 발생하면 롤백된다.")
        @Test
        void executeFailWithReturnType() {
            assertThatThrownBy(() -> transactionManager.runInTransaction(() -> {
                throw  new IllegalArgumentException();
            })).isInstanceOf(DataAccessException.class);

            assertAll(
                    () -> verify(connection).setAutoCommit(false),
                    () -> verify(connection, never()).commit(),
                    () -> verify(connection).rollback(),
                    () -> verify(connection).setAutoCommit(true)
            );
        }

        @DisplayName("반환 타입이 없는 트랜잭션 실행 메서드에서 예외가 발생하면 롤백된다.")
        @Test
        void executeFailWithVoid() {
            assertThatThrownBy(() -> transactionManager.runInTransaction(() -> {
                throw  new IllegalArgumentException();
            })).isInstanceOf(DataAccessException.class);

            assertAll(
                    () -> verify(connection).setAutoCommit(false),
                    () -> verify(connection, never()).commit(),
                    () -> verify(connection).rollback(),
                    () -> verify(connection).setAutoCommit(true)
            );
        }
    }
}
