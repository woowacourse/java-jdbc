package com.techcourse.service;

import org.h2.jdbcx.JdbcDataSource;
import java.util.UUID;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void connectionIsUnbound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void testChangePassword() {
        final var userService = new TxUserService(dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(jdbcTemplate.query("select password from user_history where user_id = ? and created_by = ? and created_at is not null",
                rs -> rs.getString("password"), 1L, createBy)).containsExactly(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select user_id from user_history", rs -> rs.getLong("user_id"))).isEmpty();
    }

    @Test
    void assembledServiceRollsBackPasswordAndHistoryAfterHistoryInsertFails() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var cause = new SQLException("이력 저장 직후 실패");
        doAnswer(invocation -> {
            final java.sql.PreparedStatement statement = (java.sql.PreparedStatement) invocation.callRealMethod();
            final String sql = invocation.getArgument(0);
            if (!sql.startsWith("insert into user_history")) {
                return statement;
            }
            final var historyStatement = spy(statement);
            doAnswer(execution -> {
                execution.callRealMethod();
                throw cause;
            }).when(historyStatement).executeUpdate();
            return historyStatement;
        }).when(connection).prepareStatement(anyString());
        final var service = new TxUserService(transactionDataSource);

        final var failure = assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "newPassword", "gugu"));

        assertThat(failure).hasCause(cause);
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select user_id from user_history", rs -> rs.getLong("user_id"))).isEmpty();
        verify(transactionDataSource).getConnection();
        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }

    @Test
    void committedChangeSucceedsWhenAutoCommitResetFails() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        doThrow(new SQLException("reset failed")).when(connection).setAutoCommit(true);
        final var userService = new TxUserService(transactionDataSource);

        assertDoesNotThrow(() -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        assertThat(jdbcTemplate.query("select password from user_history where user_id = ?",
                rs -> rs.getString("password"), 1L)).containsExactly("newPassword");
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }

    @Test
    void commitFailureIsPreservedWhenAutoCommitResetAlsoFails() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var commitFailure = new SQLException("commit failed");
        final var resetFailure = new SQLException("reset failed");
        doThrow(commitFailure).when(connection).commit();
        doThrow(resetFailure).when(connection).setAutoCommit(true);
        final var userService = new TxUserService(transactionDataSource);

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(failure.getCause()).isSameAs(commitFailure);
        assertThat(commitFailure.getSuppressed()).containsExactly(resetFailure);
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select user_id from user_history", rs -> rs.getLong("user_id"))).isEmpty();
        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
    }
}
