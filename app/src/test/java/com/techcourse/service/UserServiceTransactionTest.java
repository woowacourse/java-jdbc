package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTransactionTest {

    private Connection conn;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        UserDao userDao = mock(UserDao.class);
        conn = mock(Connection.class);
        userHistoryDao = mock(UserHistoryDao.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(userDao.findById(1L)).thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));

        userService = new UserService(userDao, userHistoryDao, dataSource);
    }

    @DisplayName("비밀번호 변경 중 이력 저장에 실패하면 롤백하고 커밋하지 않는다")
    @Test
    void rollbackWhenLogFails() throws SQLException {
        doThrow(new DataAccessException()).when(userHistoryDao).log(any());

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(conn).rollback();
        verify(conn, never()).commit();
    }

    @DisplayName("롤백도 실패하면 처음 예외를 던지고 롤백 예외는 suppressed로 남긴다")
    @Test
    void keepCauseWhenRollbackFails() throws SQLException {
        DataAccessException cause = new DataAccessException("이력 저장 실패");
        SQLException rollbackException = new SQLException("롤백 실패");
        doThrow(cause).when(userHistoryDao).log(any());
        doThrow(rollbackException).when(conn).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(cause)
                .hasSuppressedException(rollbackException);
    }
}
