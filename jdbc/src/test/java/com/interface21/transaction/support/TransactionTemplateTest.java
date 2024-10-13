package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.transaction.TransactionException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private TransactionTemplate transactionTemplate;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
    }

    @Test
    void 트랜잭션을_커밋한다() {
        // given
        TransactionCallback<Long> action = connection -> 1L;

        // when
        Object result = transactionTemplate.execute(action);

        // then
        assertAll(
                () -> verify(connection).commit(),
                () -> verify(connection, never()).rollback(),
                () -> assertThat(result).isEqualTo(1L)
        );
    }

    @Test
    void 트랜잭션을_롤백한다() {
        // given
        TransactionCallback<Void> action = connection -> {
            throw new IllegalArgumentException();
        };

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> transactionTemplate.execute(action))
                        .isExactlyInstanceOf(IllegalArgumentException.class),
                () -> verify(connection).rollback(),
                () -> verify(connection, never()).commit()
        );
    }

    @Test
    void 커밋_시_예외가_발생하면_TrasactionException_예외로_던진다() throws SQLException {
        // given
        TransactionCallback<Void> action = connection -> null;
        doThrow(new SQLException()).when(connection).commit();

        // when & then
        assertThatThrownBy(() -> transactionTemplate.execute(action))
                .isExactlyInstanceOf(TransactionException.class)
                .hasMessage("JDBC commit failed");
    }

    @Test
    void 롤백_시_예외가_발생하면_TrasactionException_예외로_던진다() throws SQLException {
        // given
        TransactionCallback<Void> action = connection -> {
            throw new IllegalArgumentException();
        };
        doThrow(new SQLException()).when(connection).rollback();

        // when & then
        assertThatThrownBy(() -> transactionTemplate.execute(action))
                .isExactlyInstanceOf(TransactionException.class)
                .hasMessage("JDBC rollback failed");
    }
}
