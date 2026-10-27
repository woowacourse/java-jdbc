package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        userService = new AppUserService(userDao, userHistoryDao);
    }

    @Test
    void findByIdReturnsDaoResult() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
    }

    @Test
    void insertPassesUserToDao() {
        final var user = new User("gugu", "password", "gugu@example.com");

        userService.insert(user);

        verify(userDao).insert(user);
        verifyNoInteractions(userHistoryDao);
    }

    @Test
    void changePasswordUpdatesUserAndRecordsChangedPassword() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        userService.changePassword(1L, "changed-password", "support");

        assertThat(user.getPassword()).isEqualTo("changed-password");
        verify(userDao).update(user);
        final ArgumentCaptor<UserHistory> history = ArgumentCaptor.forClass(UserHistory.class);
        verify(userHistoryDao).log(history.capture());
        assertThat(history.getValue().getUserId()).isEqualTo(1L);
        assertThat(history.getValue().getAccount()).isEqualTo("gugu");
        assertThat(history.getValue().getPassword()).isEqualTo("changed-password");
        assertThat(history.getValue().getEmail()).isEqualTo("gugu@example.com");
        assertThat(history.getValue().getCreateBy()).isEqualTo("support");
    }
}
