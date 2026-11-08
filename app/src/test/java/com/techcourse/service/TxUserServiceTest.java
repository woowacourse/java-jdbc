package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

        txUserService = new TxUserService(dataSource, appUserService);
    }

    @Test
    void changePasswordKeepsOriginalExceptionWhenCloseAlsoFails() throws SQLException {
        final var businessException = new DataAccessException("history 저장 실패");
        final var closeException = new SQLException("close failed");
        doThrow(businessException).when(appUserService).changePassword(anyLong(), anyString(), anyString());
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(businessException)
                .satisfies(e -> assertThat(e.getSuppressed())
                        .singleElement()
                        .satisfies(suppressed -> assertThat(suppressed).hasCause(closeException)));
        verify(connection).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void changePasswordThrowsCloseExceptionWhenOnlyCloseFails() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(CannotGetJdbcConnectionException.class);
        verify(connection).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
