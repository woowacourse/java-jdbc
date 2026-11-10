package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(userDao.findById(1L)).willReturn(Optional.of(new User(1L, "gugu", "password", "hkkang@woowahan.com")));
    }

    @Test
    void 비밀번호_변경과_이력_저장이_모두_성공하면_같은_Connection으로_커밋한다() throws SQLException {
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        userService.changePassword(1L, "qqqqq", "gugu");

        final InOrder inOrder = inOrder(connection, userDao, userHistoryDao);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(userDao).update(eq(connection), any(User.class));
        inOrder.verify(userHistoryDao).log(eq(connection), any(UserHistory.class));
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 이력_저장이_실패하면_롤백하고_예외를_다시_던진다() throws SQLException {
        final var userHistoryDao = new MockUserHistoryDao(new JdbcTemplate(dataSource));
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void commit이_실패하면_롤백하고_DataAccessException으로_바꿔_던진다() throws SQLException {
        final var commitFailure = new SQLException("commit failed");
        willThrow(commitFailure).given(connection).commit();
        final var userService = new UserService(dataSource, userDao, mock(UserHistoryDao.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(commitFailure);

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
    }

    @Test
    void commit_뒤에_Connection을_닫다_실패해도_예외를_던지지_않는다() throws SQLException {
        willThrow(new SQLException("close failed")).given(connection).close();
        final var userService = new UserService(dataSource, userDao, mock(UserHistoryDao.class));

        assertThatCode(() -> userService.changePassword(1L, "qqqqq", "gugu"))
                .doesNotThrowAnyException();

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 롤백마저_실패하면_원래_예외에_롤백_예외를_붙여_던진다() throws SQLException {
        final var rollbackFailure = new SQLException("rollback failed");
        willThrow(rollbackFailure).given(connection).rollback();
        final var userHistoryDao = new MockUserHistoryDao(new JdbcTemplate(dataSource));
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .satisfies(e -> assertThat(e.getSuppressed()).containsExactly(rollbackFailure));
        verify(connection).close();
    }
}
