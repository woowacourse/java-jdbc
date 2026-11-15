package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private Connection connection;
    private UserService appUserService;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        appUserService = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        userService = new TxUserService(appUserService, dataSource);
    }

    @Test
    void sqlExceptionInCommit() throws SQLException {
        final SQLException exception = new SQLException("커밋 실패");
        doThrow(exception).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);
        verify(connection).rollback();
    }

    @Test
    void sqlExceptionInRollback() throws SQLException {
        final DataAccessException exception = new DataAccessException("이력 저장 실패");
        final SQLException rollbackException = new SQLException("롤백 실패");
        doThrow(exception).when(appUserService).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackException).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(exception)
                .hasSuppressedException(rollbackException);
    }
}
