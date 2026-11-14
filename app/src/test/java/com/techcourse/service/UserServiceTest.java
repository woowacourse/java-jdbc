package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
            statement.execute("truncate table users restart identity");
        }
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @DisplayName("비밀번호를 변경하고 변경자와 변경 시각을 포함한 이력을 저장한다")
    @Test
    void testChangePassword() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);
        final var startedAt = LocalDateTime.now();

        userService.changePassword(1L, "qqqqq", "admin");

        final var finishedAt = LocalDateTime.now();
        assertThat(userService.findById(1L).getPassword()).isEqualTo("qqqqq");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select * from user_history")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(1L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo("qqqqq");
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isBetween(startedAt.minusNanos(1_000), finishedAt.plusNanos(1_000));
            assertThat(resultSet.next()).isFalse();
        }
    }

    @DisplayName("존재하지 않는 사용자의 비밀번호를 변경하면 예외가 발생하고 이력이 남지 않는다")
    @Test
    void rejectsPasswordChangeForMissingUser() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(999L, "newPassword", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    @DisplayName("이력 저장 중 예외가 발생하면 비밀번호 변경을 롤백한다")
    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    @DisplayName("이력 저장 쿼리가 실패하면 비밀번호 변경을 롤백한다")
    @Test
    void rollsBackWhenHistoryInsertFails() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    @DisplayName("이력 저장 후 예외가 발생하면 비밀번호와 이력을 함께 롤백한다")
    @Test
    void rollsBackPasswordAndHistoryWhenFailureOccursAfterInsert() {
        final var cause = new IllegalStateException("이력 저장 후 오류가 발생했습니다.");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw cause;
            }
        };
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "admin"))
                .isSameAs(cause);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    @DisplayName("트랜잭션 종료 후 기존 자동 커밋 설정을 복구하고 연결을 닫는다")
    @Test
    void restoresOriginalAutoCommitAndClosesConnection() throws SQLException {
        final var transactionDataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var transactionUserDao = mock(UserDao.class);
        final var userHistoryDao = mock(UserHistoryDao.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(transactionUserDao.findById(connection, 1L))
                .thenReturn(new User(1L, "gugu", "password", "gugu@example.com"));
        final var userService = new UserService(transactionDataSource, transactionUserDao, userHistoryDao);

        userService.changePassword(1L, "newPassword", "admin");

        verify(connection).commit();
        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @DisplayName("커밋이 실패하면 롤백하고 연결을 닫는다")
    @Test
    void rollsBackWhenCommitFails() throws SQLException {
        final var transactionDataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var transactionUserDao = mock(UserDao.class);
        final var cause = new SQLException("트랜잭션 커밋에 실패했습니다.");
        when(transactionDataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(transactionUserDao.findById(connection, 1L))
                .thenReturn(new User(1L, "gugu", "password", "gugu@example.com"));
        doThrow(cause).when(connection).commit();
        final var userService = new UserService(transactionDataSource, transactionUserDao, mock(UserHistoryDao.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @DisplayName("롤백이 실패해도 원래 예외를 보존하고 연결을 닫는다")
    @Test
    void preservesOriginalExceptionWhenRollbackFails() throws SQLException {
        final var transactionDataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var transactionUserDao = mock(UserDao.class);
        final var cause = new DataAccessException("사용자 조회에 실패했습니다.");
        final var rollbackFailure = new SQLException("트랜잭션 롤백에 실패했습니다.");
        when(transactionDataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(transactionUserDao.findById(connection, 1L)).thenThrow(cause);
        doThrow(rollbackFailure).when(connection).rollback();
        final var userService = new UserService(transactionDataSource, transactionUserDao, mock(UserHistoryDao.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "admin"))
                .isSameAs(cause);

        assertThat(cause.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    private void assertNoHistory() {
        assertThat(jdbcTemplate.query("select id from user_history", row -> row.getLong("id"))).isEmpty();
    }
}
