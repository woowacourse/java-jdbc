package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceLookupTest {

    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final UserService userService = new UserService(userDao, userHistoryDao, dataSource);

    @Test
    void returnsExistingUser() {
        final var user = new User(1L, "account", "password", "user@example.com");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.findById(1L)).isSameAs(user);
    }

    @Test
    void rejectsMissingUser() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("사용자를 찾을 수 없습니다. id: 999");
    }

    @Test
    void doesNotUpdateOrWriteHistoryForMissingUser() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(999L, "new-password", "admin"))
                .isInstanceOf(NoSuchElementException.class);

        verify(userDao, never()).update(any(Connection.class), any(User.class));
        verifyNoInteractions(userHistoryDao, dataSource);
    }
}
