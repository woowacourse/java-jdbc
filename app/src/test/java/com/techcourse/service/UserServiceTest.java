package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.sql.SQLException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private javax.sql.DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @AfterEach
    void connectionIsNotBoundAfterTest() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "support-agent";
        final var startedAt = LocalDateTime.now().minusSeconds(1);
        userService.changePassword(1L, newPassword, createBy);
        final var finishedAt = LocalDateTime.now().plusSeconds(1);

        final var actual = userService.findById(1L);
        final var history = jdbcTemplate.queryForObject(
                "select user_id, password, created_by, created_at from user_history",
                resultSet -> new HistoryRecord(
                        resultSet.getLong("user_id"),
                        resultSet.getString("password"),
                        resultSet.getString("created_by"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()));

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(history).isNotNull();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.createdBy()).isEqualTo(createBy);
        assertThat(history.createdAt()).isBetween(startedAt, finishedAt);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testRollbackAndCleanupWhenConnectionCloseAlsoFails() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final var transactionTemplate = new JdbcTemplate(transactionDataSource);
        final var appUserService = new AppUserService(
                new UserDao(transactionTemplate), new MockUserHistoryDao(transactionTemplate));
        final var userService = new TxUserService(transactionDataSource, appUserService);
        final var closeFailure = new SQLException("Close failed");
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw closeFailure;
        }).when(connection).close();

        final var exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        verify(connection).rollback();
        verify(connection).close();
        assertThat(exception.getSuppressed()).hasSize(1);
        assertThat(exception.getSuppressed()[0]).isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
        assertThat(TransactionSynchronizationManager.getResource(transactionDataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testTransactionRollsBackWhenUserUpdateAffectsNoRows() {
        final var userDaoWithNoUpdatedRows = new UserDao(jdbcTemplate) {
            @Override
            public int update(final User user) {
                jdbcTemplate.update("delete from users where id = ?", user.getId());
                return 0;
            }
        };
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDaoWithNoUpdatedRows, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "support-agent"));

        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testSaveAndFindByIdThroughTransactionService() {
        final UserService userService = new TxUserService(
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));
        final var user = new User("new-user", "password", "new-user@example.com");

        userService.save(user);

        final var savedUser = userDao.findByAccount("new-user");
        final var actual = userService.findById(savedUser.getId());
        assertThat(actual.getAccount()).isEqualTo("new-user");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.getEmail()).isEqualTo("new-user@example.com");
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", resultSet -> resultSet.getInt(1));
    }

    private record HistoryRecord(long userId, String password, String createdBy, LocalDateTime createdAt) {}
}
