package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AppUserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserHistoryDao userHistoryDao;

    @InjectMocks
    private AppUserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changePassword() {
        // given
        final var userId = 1L;
        final var newPassword = "qqqqq";
        final var createdBy = "gugu";
        final var user = new User(userId, "gugu", "oldPassword", "gugu@example.com");

        when(userDao.findById(userId)).thenReturn(user);

        // when
        userService.changePassword(userId, newPassword, createdBy);

        // then
        assertAll(
                () -> assertThat(user.getPassword()).isEqualTo(newPassword),
                () -> verify(userDao).update(user),
                () -> verify(userHistoryDao).log(any(UserHistory.class))
        );
    }
}
