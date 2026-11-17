package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");
        jdbcTemplate.update("truncate table user_history restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void testSave() {
        final var appUserService = new AppUserService(userDao, new UserHistoryDao(jdbcTemplate));
        final UserService userService = new TxUserService(appUserService);

        userService.save(new User("juni", "password", "juni@example.com"));

        assertThat(userService.findById(2L).getAccount()).isEqualTo("juni");
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final Long historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and account = ? "
                        + "and password = ? and email = ? and created_by = ? and created_at is not null",
                resultSet -> resultSet.getLong(1),
                actual.getId(), actual.getAccount(), newPassword, actual.getEmail(), createBy);
        assertThat(historyCount).isEqualTo(1L);
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
        final Long historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    @Test
    void testTransactionRollbackOnHistoryInsertFailure() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final Long historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    @Test
    void transactionUsesOneConnectionAndReleasesIt() throws SQLException {
        final var dataSource = mock(DataSource.class);
        try (final var connection = spy(DataSourceConfig.getInstance().getConnection())) {
            when(dataSource.getConnection()).thenReturn(connection);
            final var template = new JdbcTemplate(dataSource);
            final var appUserService = new AppUserService(new UserDao(template), new UserHistoryDao(template));
            final var userService = new TxUserService(appUserService, dataSource);

            userService.changePassword(1L, "newPassword", "gugu");

            verify(dataSource).getConnection();
            verify(connection, atLeastOnce()).commit();
            verify(connection).setAutoCommit(true);
            assertThat(connection.isClosed()).isTrue();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }
    }

    @Test
    void rollbackReleasesConnectionAndSynchronization() throws SQLException {
        final var dataSource = mock(DataSource.class);
        try (final var connection = spy(DataSourceConfig.getInstance().getConnection())) {
            when(dataSource.getConnection()).thenReturn(connection);
            final var template = new JdbcTemplate(dataSource);
            final var appUserService = new AppUserService(new UserDao(template), new MockUserHistoryDao(template));
            final var userService = new TxUserService(appUserService, dataSource);

            assertThrows(DataAccessException.class,
                    () -> userService.changePassword(1L, "newPassword", "gugu"));

            verify(dataSource).getConnection();
            verify(connection).rollback();
            verify(connection).setAutoCommit(true);
            assertThat(connection.isClosed()).isTrue();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        }
    }
}
