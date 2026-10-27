package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    @Test
    @DisplayName("일반 조회와 저장은 Connection 없이 DAO에 위임한다")
    void findAndSaveDelegateToTheUserDao() {
        final var userDao = mock(UserDao.class);
        final var historyDao = mock(UserHistoryDao.class);
        final var service = new AppUserService(userDao, historyDao);
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        assertThat(service.findById(1L)).isSameAs(user);
        service.save(user);

        verify(userDao).findById(1L);
        verify(userDao).insert(user);
        verifyNoInteractions(historyDao);
    }

    @Test
    @DisplayName("비밀번호 변경 후 사용자 수정과 변경 이력 저장을 순서대로 위임한다")
    void changesThePasswordAndRecordsTheChangedUser() {
        final var userDao = mock(UserDao.class);
        final var historyDao = mock(UserHistoryDao.class);
        final var service = new AppUserService(userDao, historyDao);
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        final var historyCaptor = ArgumentCaptor.forClass(UserHistory.class);
        when(userDao.findById(1L)).thenReturn(user);

        service.changePassword(1L, "newPassword", "operator");

        assertThat(user.getPassword()).isEqualTo("newPassword");
        final var order = inOrder(userDao, historyDao);
        order.verify(userDao).findById(1L);
        order.verify(userDao).update(user);
        order.verify(historyDao).log(historyCaptor.capture());
        final var history = historyCaptor.getValue();
        assertThat(history.getUserId()).isEqualTo(user.getId());
        assertThat(history.getPassword()).isEqualTo("newPassword");
        assertThat(history.getCreateBy()).isEqualTo("operator");
    }

    @Test
    @DisplayName("일반 조회의 null 계약은 유지하고 없는 사용자의 변경만 명확한 예외로 거절한다")
    void missingUserCannotBeChangedOrRecorded() {
        final var userDao = mock(UserDao.class);
        final var historyDao = mock(UserHistoryDao.class);
        final var service = new AppUserService(userDao, historyDao);

        assertThat(service.findById(404L)).isNull();
        final var failure = assertThrows(UserNotFoundException.class,
                () -> service.changePassword(404L, "newPassword", "operator"));

        assertThat(failure).hasMessage("User not found: 404");
        verify(userDao, never()).update(any(User.class));
        verifyNoInteractions(historyDao);
    }
}
