package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService appUserService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        appUserService = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);

        txUserService = new TxUserService(appUserService, dataSource);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 커밋하면_autoCommit을_원래대로_되돌리고_Connection을_닫는다() throws SQLException {
        txUserService.changePassword(1L, "newPassword", "gugu");

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    void 롤백해도_autoCommit을_원래대로_되돌리고_Connection을_닫는다() throws SQLException {
        doThrow(new DataAccessException()).when(appUserService).changePassword(anyLong(), anyString(), anyString());

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    void autoCommit이_원래_꺼져_있으면_변경하지_않는다() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        txUserService.changePassword(1L, "newPassword", "gugu");

        verify(connection, never()).setAutoCommit(anyBoolean());
        verify(connection).commit();
    }

    @Test
    void autoCommit_복원에_실패해도_원래_예외를_던지고_Connection을_닫는다() throws SQLException {
        final var cause = new DataAccessException("change failed");
        doThrow(cause).when(appUserService).changePassword(anyLong(), anyString(), anyString());
        doThrow(new SQLException("restore failed")).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(cause);

        verify(connection).close();
    }
}
