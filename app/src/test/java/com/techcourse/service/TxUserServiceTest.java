package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

    @Test
    void rollbackWhenChangePasswordFails() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        final var appUserService = mock(UserService.class);
        doThrow(new DataAccessException()).when(appUserService).changePassword(1L, "newPassword", "gugu");
        final var userService = new TxUserService(dataSource, appUserService);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }
}
