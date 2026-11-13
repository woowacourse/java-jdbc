package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        userService = new AppUserService(userDao, userHistoryDao);
    }

    @Test
    void findsUserThroughDao() {
        final var user = new User(1L, "gugu", "password", "gugu@woowahan.com");
        when(userDao.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);

        verify(userDao).findById(1L);
        verifyNoInteractions(userHistoryDao);
    }

    @Test
    void savesUserThroughDao() {
        final var user = new User("gugu", "password", "gugu@woowahan.com");

        userService.save(user);

        verify(userDao).insert(user);
        verifyNoInteractions(userHistoryDao);
    }

    @Test
    void changesPasswordAndRecordsUpdatedUser() {
        final var user = new User(1L, "gugu", "password", "gugu@woowahan.com");
        when(userDao.findById(1L)).thenReturn(user);

        userService.changePassword(1L, "new-password", "editor");

        assertThat(user.getPassword()).isEqualTo("new-password");
        final var historyCaptor = ArgumentCaptor.forClass(UserHistory.class);
        final var order = inOrder(userDao, userHistoryDao);
        order.verify(userDao).findById(1L);
        order.verify(userDao).update(user);
        order.verify(userHistoryDao).log(historyCaptor.capture());
        final var history = historyCaptor.getValue();
        assertThat(history.getUserId()).isEqualTo(1L);
        assertThat(history.getAccount()).isEqualTo("gugu");
        assertThat(history.getPassword()).isEqualTo("new-password");
        assertThat(history.getEmail()).isEqualTo("gugu@woowahan.com");
        assertThat(history.getCreateBy()).isEqualTo("editor");
    }
}
