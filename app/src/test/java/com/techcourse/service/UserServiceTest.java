package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private UserDao userDao;
    private long userId;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();

        DatabasePopulatorUtils.execute(dataSource);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("delete from user_history");
        jdbcTemplate.update("delete from users");

        final var user = new User(
                "gugu",
                "password",
                "hkkang@woowahan.com"
        );

        userDao.insert(user);
        this.userId = userDao.findByAccount("gugu").getId();
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(userId, newPassword, createBy);

        final var actual = userService.findById(userId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                """ 
                select count(*) 
                from user_history 
                where user_id = ? 
                  and password = ? 
                  and created_by = ? 
                  and created_at is not null 
                """,
                resultSet -> resultSet.getLong(1),
                userId,
                newPassword,
                createBy
        );

        assertThat(historyCount).isEqualTo(1L);
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var originalPassword = userService.findById(userId).getPassword();
        final var newPassword = "newPassword";
        final var createBy = "gugu";

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(userId, newPassword, createBy));

        final var actual = userService.findById(userId);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ?",
                resultSet -> resultSet.getLong(1),
                userId
        );

        assertThat(historyCount).isZero();
    }

    @Test
    void rollsBackWithAnExplicitlyConfiguredDataSource() throws SQLException {
        final var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenAnswer(invocation -> DataSourceConfig.getInstance().getConnection());
        final var template = new JdbcTemplate(dataSource);
        final var service = new TxUserService(new AppUserService(
                new UserDao(template), new MockUserHistoryDao(template)), dataSource);
        final var originalPassword = service.findById(userId).getPassword();

        assertThrows(DataAccessException.class,
                () -> service.changePassword(userId, "newPassword", "gugu"));

        assertThat(service.findById(userId).getPassword()).isEqualTo(originalPassword);
        final var historyCount = template.queryForObject(
                "select count(*) from user_history where user_id = ?",
                resultSet -> resultSet.getLong(1), userId);
        assertThat(historyCount).isZero();
    }

    @Test
    void restoresAutoCommitBeforeClosingAfterCommit() throws SQLException {
        verifyAutoCommitRestored(false, true);
    }

    @Test
    void restoresAutoCommitBeforeClosingAfterRollback() throws SQLException {
        verifyAutoCommitRestored(true, true);
    }

    @Test
    void preservesInitiallyDisabledAutoCommit() throws SQLException {
        verifyAutoCommitRestored(false, false);
    }

    @Test
    void doesNotEnableAutoCommitWhenRollbackFails() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            final var borrowedConnection = spy(connection);
            final var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenReturn(borrowedConnection);
            doAnswer(invocation -> null).when(borrowedConnection).close();
            final var rollbackFailure = new SQLException("rollback failed");
            doThrow(rollbackFailure).when(borrowedConnection).rollback();
            final var transactionalTemplate = new JdbcTemplate(dataSource);
            final var appUserService = new AppUserService(
                    new UserDao(transactionalTemplate), new MockUserHistoryDao(transactionalTemplate));
            final var service = new TxUserService(appUserService, dataSource);

            final var failure = assertThrows(DataAccessException.class,
                    () -> service.changePassword(userId, "newPassword", "gugu"));

            assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
            verify(borrowedConnection, never()).setAutoCommit(true);
            verify(borrowedConnection).close();
            connection.rollback();
        }
    }

    private void verifyAutoCommitRestored(final boolean fails, final boolean originalAutoCommit) throws SQLException {
        final var connection = spy(DataSourceConfig.getInstance().getConnection());
        connection.setAutoCommit(originalAutoCommit);
        final var dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        doAnswer(invocation -> {
            assertThat(connection.getAutoCommit()).isEqualTo(originalAutoCommit);
            return invocation.callRealMethod();
        }).when(connection).close();
        final var transactionalTemplate = new JdbcTemplate(dataSource);
        final UserHistoryDao historyDao = fails
                ? new MockUserHistoryDao(transactionalTemplate)
                : new UserHistoryDao(transactionalTemplate);
        final var appUserService = new AppUserService(new UserDao(transactionalTemplate), historyDao);
        final var service = new TxUserService(appUserService, dataSource);

        try {
            if (fails) {
                assertThrows(DataAccessException.class,
                        () -> service.changePassword(userId, "newPassword", "gugu"));
            } else {
                service.changePassword(userId, "newPassword", "gugu");
            }
            verify(connection).close();
        } finally {
            if (!connection.isClosed()) {
                connection.rollback();
                connection.close();
            }
        }
    }
}
