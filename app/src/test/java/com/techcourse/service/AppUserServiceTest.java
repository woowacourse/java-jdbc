package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AppUserServiceTest {

    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
    private final UserService userService = new AppUserService(userDao, userHistoryDao);

    @Test
    void findsUserById() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
    }

    @Test
    void savesUser() {
        final var user = new User("gugu", "password", "gugu@example.com");

        userService.save(user);

        verify(userDao).insert(user);
    }

    @Test
    void changesPasswordAndRecordsHistory() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        userService.changePassword(1L, "newPassword", "admin");

        final var history = ArgumentCaptor.forClass(UserHistory.class);
        final var order = inOrder(userDao, userHistoryDao);
        order.verify(userDao).findById(1L);
        order.verify(userDao).update(user);
        order.verify(userHistoryDao).log(history.capture());
        assertThat(user.getPassword()).isEqualTo("newPassword");
        assertThat(history.getValue().getUserId()).isEqualTo(1L);
        assertThat(history.getValue().getPassword()).isEqualTo("newPassword");
        assertThat(history.getValue().getCreateBy()).isEqualTo("admin");
    }
}
