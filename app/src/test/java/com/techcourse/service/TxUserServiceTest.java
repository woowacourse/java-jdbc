package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TxUserServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService delegate = mock(UserService.class);
    private final TxUserService service = new TxUserService(new TransactionTemplate(dataSource), delegate);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @Test
    void changePasswordDelegatesInsideTransaction() throws SQLException {
        final List<Connection> boundDuringDelegate = new ArrayList<>();
        doAnswer(invocation -> boundDuringDelegate.add(TransactionSynchronizationManager.getResource(dataSource)))
                .when(delegate).changePassword(1L, "newPassword", "gugu");

        service.changePassword(1L, "newPassword", "gugu");

        assertThat(boundDuringDelegate).containsExactly(connection);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void changePasswordFailureRollsBackAndRethrowsSameException() throws SQLException {
        final var failure = new DataAccessException("history failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "gugu")).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void readAndSaveAreDelegatedWithoutStartingTransaction() throws SQLException {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(delegate.findById(1L)).thenReturn(user);

        assertThat(service.findById(1L)).isSameAs(user);
        service.save(user);

        verify(delegate).save(user);
        verify(dataSource, never()).getConnection();
    }
}
