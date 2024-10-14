package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        transactionTemplate = new TransactionTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("트랜잭션이 성공하면 커밋된다.")
    @Test
    void commitOnSuccess() {
        // given
        TransactionExecutor executor = conn -> {
        };

        // when
        transactionTemplate.execute(executor);

        // then
        assertAll(
                () -> verify(connection).commit(),
                () -> verify(connection, never()).rollback()
        );
    }

    @DisplayName("트랜잭션 내에서 예외가 발생하면 롤백한다.")
    @Test
    void rollbackOnFailure() {
        // given
        TransactionExecutor executor = conn -> {
            throw new RuntimeException();
        };

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> transactionTemplate.execute(executor))
                        .isInstanceOf(RuntimeException.class),
                () -> verify(connection).rollback(),
                () -> verify(connection, never()).commit()
        );
    }
}
