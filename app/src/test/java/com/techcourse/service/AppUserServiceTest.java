package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("Connection에 의존하지 않는 사용자 업무 서비스")
class AppUserServiceTest {

    private UserDao userDao;
    private UserHistoryDao historyDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        historyDao = mock(UserHistoryDao.class);
        userService = new AppUserService(userDao, historyDao);
    }

    @Test
    @DisplayName("사용자 조회를 DAO에 위임한다")
    void findsUserThroughDao() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
        verifyNoInteractions(historyDao);
    }

    @Test
    @DisplayName("사용자 저장을 DAO에 위임한다")
    void savesUserThroughDao() {
        final var user = new User("gugu", "password", "gugu@example.com");

        userService.save(user);

        verify(userDao).insert(user);
        verifyNoInteractions(historyDao);
    }

    @Test
    @DisplayName("사용자의 비밀번호를 바꾼 뒤 변경된 정보와 변경자로 이력을 남긴다")
    void changesPasswordAndRecordsHistoryWithoutPassingConnection() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(userDao.findById(1L)).thenReturn(user);

        userService.changePassword(1L, "new-password", "admin");

        final var history = ArgumentCaptor.forClass(UserHistory.class);
        final var order = inOrder(userDao, historyDao);
        order.verify(userDao).findById(1L);
        order.verify(userDao).update(user);
        order.verify(historyDao).log(history.capture());
        assertThat(user.getPassword()).isEqualTo("new-password");
        assertThat(history.getValue().getPassword()).isEqualTo("new-password");
        assertThat(history.getValue().getUserId()).isEqualTo(1L);
        assertThat(history.getValue().getCreateBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("사용자 수정이 실패하면 이력을 저장하지 않고 원래 예외를 전달한다")
    void stopsBeforeHistoryWhenUpdateFails() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        final var failure = new IllegalStateException("수정 실패");
        when(userDao.findById(1L)).thenReturn(user);
        doThrow(failure).when(userDao).update(user);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);
        verifyNoInteractions(historyDao);
    }
}
