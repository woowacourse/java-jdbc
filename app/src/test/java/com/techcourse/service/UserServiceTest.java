package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
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
        final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
        final var transactionUserDao = new UserDao(transactionJdbcTemplate);
        final var userHistoryDao = new UserHistoryDao(transactionJdbcTemplate);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

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
            }).when(transactionUserDao).update(any(User.class));

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
    void 사용자_수정이_0건이면_이력을_저장하지_않고_롤백한다() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var transactionJdbcTemplate = spy(new JdbcTemplate(transactionDataSource));
        doReturn(0).when(transactionJdbcTemplate).update(anyString(), any(Object[].class));
        final var transactionUserDao = new UserDao(transactionJdbcTemplate);
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);
        final var originalPassword = userDao.findById(1L).getPassword();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("사용자 수정 결과는 1건이어야 합니다. 실제 수정 건수: 0");

        verifyNoInteractions(userHistoryDao);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
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

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
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
        final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
        final var transactionUserDao = new UserDao(transactionJdbcTemplate);
        final var userHistoryDao = new UserHistoryDao(transactionJdbcTemplate);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);
        final var originalPassword = userDao.findById(1L).getPassword();

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(exception).hasCause(commitException);
        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
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
        when(transactionUserDao.findByIdForUpdate(1L)).thenReturn(user);
        final var updateException = new DataAccessException("사용자 수정 실패");
        doThrow(updateException).when(transactionUserDao).update(user);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(exception).isSameAs(updateException);
        assertThat(exception.getSuppressed()).containsExactly(rollbackException);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }

    @Test
    void 비밀번호_변경은_하나의_연결을_공유하고_완료_후_등록을_해제한다() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
        final var transactionUserDao = spy(new UserDao(transactionJdbcTemplate));
        final var userHistoryDao = spy(new UserHistoryDao(transactionJdbcTemplate));
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isSameAs(connection);
            assertThat(connection.isClosed()).isFalse();
            return invocation.callRealMethod();
        }).when(userHistoryDao).log(any(UserHistory.class));

        userService.changePassword(1L, "newPassword", "gugu");

        verify(transactionDataSource).getConnection();
        verify(transactionUserDao).findByIdForUpdate(1L);
        verify(transactionUserDao).update(any(User.class));
        verify(userHistoryDao).log(any(UserHistory.class));
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isEqualTo(1L);
    }

    @Test
    void 자동_커밋_설정에_실패하면_DAO를_호출하지_않고_연결을_닫는다() throws SQLException {
        final var connection = mock(Connection.class);
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var settingException = new SQLException("자동 커밋 설정 실패");
        doThrow(settingException).when(connection).setAutoCommit(false);
        final var transactionUserDao = mock(UserDao.class);
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(settingException);

        verifyNoInteractions(transactionUserDao, userHistoryDao);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }

    @Test
    void 사용자_수정과_연결_종료가_함께_실패해도_원래_예외를_보존하고_등록을_해제한다() throws SQLException {
        final var connection = mock(Connection.class);
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var transactionUserDao = mock(UserDao.class);
        final var user = userDao.findById(1L);
        when(transactionUserDao.findByIdForUpdate(1L)).thenReturn(user);
        final var updateException = new DataAccessException("사용자 수정 실패");
        doThrow(updateException).when(transactionUserDao).update(user);
        final var closeException = new SQLException("연결 종료 실패");
        doThrow(closeException).when(connection).close();
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(updateException)
                .hasSuppressedException(closeException);

        verifyNoInteractions(userHistoryDao);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }
}
