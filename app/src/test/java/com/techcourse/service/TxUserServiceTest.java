package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private TxUserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        userService = new TxUserService(dataSource, delegate);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void preservesBusinessFailureWhenRollbackAlsoFails() throws SQLException {
        final var originalFailure = new DataAccessException("History insert failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(originalFailure).when(delegate).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackFailure).when(connection).rollback();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(originalFailure);
        assertThat(actual.getSuppressed()).containsExactly(rollbackFailure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void preservesCommitFailureWhenRollbackAlsoFails() throws SQLException {
        final var originalFailure = new SQLException("Commit failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(originalFailure).when(connection).commit();
        doThrow(rollbackFailure).when(connection).rollback();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(originalFailure);
        assertThat(originalFailure.getSuppressed()).containsExactly(rollbackFailure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }
}
