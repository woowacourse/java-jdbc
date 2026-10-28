package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("""
                truncate table users restart identity
                """);
        jdbcTemplate.update("""
                truncate table user_history restart identity
                """);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var histories = jdbcTemplate.query("""
                select user_id, password, created_by
                from user_history
                """, resultSet -> List.of(
                resultSet.getLong("user_id"),
                resultSet.getString("password"),
                resultSet.getString("created_by")
        ));

        assertThat(histories).containsExactly(List.of(1L, newPassword, createBy));
    }

    @Test
    void 비밀번호_변경_시작_전에_저장된_이메일을_유지한다() throws SQLException {
        final var transactionDataSource = mock(DataSource.class);
        final var newEmail = "new@woowahan.com";
        when(transactionDataSource.getConnection()).thenAnswer(invocation -> {
            jdbcTemplate.update("update users set email = ? where id = ?", newEmail, 1L);
            return dataSource.getConnection();
        });
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(transactionDataSource, userDao, userHistoryDao);

        userService.changePassword(1L, "newPassword", "gugu");

        final var actual = userDao.findById(1L);
        assertThat(actual.getEmail()).isEqualTo(newEmail);
        assertThat(actual.getPassword()).isEqualTo("newPassword");
        final var historyEmail = jdbcTemplate.queryForObject(
                "select email from user_history where user_id = ?",
                resultSet -> resultSet.getString("email"), 1L);
        assertThat(historyEmail).isEqualTo(newEmail);
    }

    @Test
    void 비밀번호_변경_중에는_다른_연결의_이메일_수정을_막는다() throws SQLException {
        final var transactionUserDao = spy(userDao);
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, transactionUserDao, userHistoryDao);
        final var user = userDao.findById(1L);
        final var newEmail = "new@woowahan.com";

        try (final var otherConnection = dataSource.getConnection()) {
            jdbcTemplate.update(otherConnection, "set lock_timeout 100");
            doAnswer(invocation -> {
                assertThatThrownBy(() -> jdbcTemplate.update(otherConnection,
                        "update users set email = ? where id = ?", newEmail, 1L))
                        .isInstanceOf(DataAccessException.class)
                        .hasCauseInstanceOf(SQLTimeoutException.class);
                return invocation.callRealMethod();
            }).when(transactionUserDao).update(any(Connection.class), any(User.class));

            userService.changePassword(1L, "newPassword", "gugu");

            jdbcTemplate.update(otherConnection, "update users set email = ? where id = ?", newEmail, 1L);
        }

        final var actual = userDao.findById(1L);
        assertThat(actual.getEmail()).isEqualTo(newEmail);
        assertThat(actual.getPassword()).isEqualTo("newPassword");
        final var historyEmail = jdbcTemplate.queryForObject(
                "select email from user_history where user_id = ?",
                resultSet -> resultSet.getString("email"), 1L);
        assertThat(historyEmail).isEqualTo(user.getEmail());
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);
        final var originalPassword = userService.findById(1L).getPassword();

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);

        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isZero();
    }

    @Test
    void 이력_저장_SQL이_실패하면_비밀번호를_되돌린다() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);
        final var originalPassword = userService.findById(1L).getPassword();

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(exception).hasCauseInstanceOf(SQLException.class);
        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isZero();
    }

    @Test
    void 커밋에_실패하면_비밀번호와_이력을_롤백한다() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var commitException = new SQLException("커밋 실패");
        doThrow(commitException).when(connection).commit();
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(transactionDataSource, userDao, userHistoryDao);
        final var originalPassword = userService.findById(1L).getPassword();

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(exception).hasCause(commitException);
        verify(connection).rollback();
        verify(connection).close();
        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isZero();
    }

    @Test
    void 롤백에_실패해도_원래_예외를_보존하고_연결을_닫는다() throws SQLException {
        final var connection = mock(Connection.class);
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var rollbackException = new SQLException("롤백 실패");
        doThrow(rollbackException).when(connection).rollback();
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var transactionUserDao = mock(UserDao.class);
        final var user = userDao.findById(1L);
        when(transactionUserDao.findByIdForUpdate(connection, 1L)).thenReturn(user);
        final var updateException = new DataAccessException("사용자 수정 실패");
        doThrow(updateException).when(transactionUserDao).update(connection, user);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(exception).isSameAs(updateException);
        assertThat(exception.getSuppressed()).containsExactly(rollbackException);
        verify(connection).close();
    }
}
