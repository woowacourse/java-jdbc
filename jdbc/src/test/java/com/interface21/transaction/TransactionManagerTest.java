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
import java.util.function.Consumer;
import java.util.function.Function;
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
            Function<Connection, String> functionResult = connection -> "kaki";

            String result = transactionManager.runInTransaction(functionResult);

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
            Consumer<Connection> consumer = connection -> consumerResults[0] = true;

            transactionManager.runInTransaction(consumer);

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
            Function<Connection, String> functionResult = connection -> {
                throw new IllegalArgumentException();
            };

            assertThatThrownBy(() -> transactionManager.runInTransaction(functionResult))
                    .isInstanceOf(DataAccessException.class);

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
            Consumer<Connection> consumer = connection -> {
                throw new IllegalArgumentException();
            };

            assertThatThrownBy(() -> transactionManager.runInTransaction(consumer))
                    .isInstanceOf(DataAccessException.class);

            assertAll(
                    () -> verify(connection).setAutoCommit(false),
                    () -> verify(connection, never()).commit(),
                    () -> verify(connection).rollback(),
                    () -> verify(connection).setAutoCommit(true)
            );
        }
    }
}
