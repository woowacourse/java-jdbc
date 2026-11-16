package com.interface21.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void returnsTheCallbackResultWhenClosingFailsAfterCommit() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        final var result = transactionTemplate.execute(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return "result";
        });

        assertThat(result).isEqualTo("result");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).close();
        verifyNoMoreInteractions(connection);
    }

    @Test
    void retainsTheCallbackFailureWhenClosingAlsoFails() throws SQLException {
        final var callbackFailure = new IllegalStateException("callback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(closeFailure).when(connection).close();

        final var actual = assertThrows(IllegalStateException.class, () ->
                transactionTemplate.executeWithoutResult(() -> {
                    throw callbackFailure;
                }));

        assertThat(actual).isSameAs(callbackFailure);
        assertThat(actual.getSuppressed()).singleElement()
                .satisfies(suppressed -> assertThat(suppressed.getCause()).isSameAs(closeFailure));
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).rollback();
        order.verify(connection).close();
        verifyNoMoreInteractions(connection);
    }
}
