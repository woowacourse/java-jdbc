package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TxUserServiceTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService delegate = mock(UserService.class);
    private final TxUserService service = new TxUserService(delegate, dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @Test
    void commitsAndRestoresConnectionAfterDelegateReturns() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "new", "gugu");

        service.changePassword(1L, "new", "gugu");

        final var order = inOrder(connection, delegate);
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(1L, "new", "gugu");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollsBackAndPreservesOriginalFailureWhenCleanupFails() throws SQLException {
        final var failure = new IllegalArgumentException("business failure");
        final var rollbackFailure = new SQLException("rollback failure");
        final var closeFailure = new SQLException("close failure");
        doThrow(failure).when(delegate).changePassword(1L, "new", "gugu");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> service.changePassword(1L, "new", "gugu")).isSameAs(failure);

        assertThat(failure.getSuppressed()).hasSize(2);
        assertThat(failure.getSuppressed()[0]).isSameAs(rollbackFailure);
        assertThat(failure.getSuppressed()[1]).hasCause(closeFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void commitFailureRollsBackAndReleasesConnection() throws SQLException {
        final var failure = new SQLException("commit failure");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> service.changePassword(1L, "new", "gugu"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void beginFailureClosesConnectionWithoutInvokingBusinessLogic() throws SQLException {
        doThrow(new SQLException("begin failure")).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> service.changePassword(1L, "new", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verifyNoInteractions(delegate);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void delegatesFindAndSave() {
        final var user = new User("gugu", "password", "gugu@example.com");
        when(delegate.findById(1L)).thenReturn(user);
        assertThat(service.findById(1L)).isSameAs(user);
        service.save(user);
        verify(delegate).save(user);
        verifyNoInteractions(dataSource);
    }

    @Test
    void doesNotTakeOwnershipOfAnExistingTransaction() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> service.changePassword(1L, "new", "gugu"))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            verifyNoInteractions(delegate, dataSource);
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
