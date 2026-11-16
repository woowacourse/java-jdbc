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
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao));

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao));

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void testTransactionRollbackWhenCommitFails() {
        final var failingDataSource = dataSourceFailingOn((name, args) -> "commit".equals(name), new ArrayList<>());
        final var failingTemplate = new JdbcTemplate(failingDataSource);
        final var userService = new TxUserService(failingDataSource,
                new AppUserService(new UserDao(failingTemplate), new UserHistoryDao(failingTemplate)));
        final var id = lastUserId();
        final var passwordBefore = userDao.findById(id).getPassword();
        final var historyCountBefore = countHistories();

        assertThrows(DataAccessException.class, () -> userService.changePassword(id, "newPassword", "gugu"));

        assertThat(userDao.findById(id).getPassword()).isEqualTo(passwordBefore);
        assertThat(countHistories()).isEqualTo(historyCountBefore);
        assertThat(TransactionSynchronizationManager.getResource(failingDataSource)).isNull();
    }

    @Test
    void testChangePasswordWithSingleConnectionPool() {
        final var pool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "", "");
        pool.setMaxConnections(1);
        pool.setLoginTimeout(1);
        try {
            final var poolTemplate = new JdbcTemplate(pool);
            final var userService = new TxUserService(pool,
                    new AppUserService(new UserDao(poolTemplate), new UserHistoryDao(poolTemplate)));
            final var id = lastUserId();

            assertThatCode(() -> userService.changePassword(id, "qqqqq", "gugu")).doesNotThrowAnyException();

            assertThat(userService.findById(id).getPassword()).isEqualTo("qqqqq");
            assertThat(pool.getActiveConnections()).isZero();
        } finally {
            pool.dispose();
        }
    }

    @Test
    void testChangePasswordKeepsResultWhenAutoCommitRestoreFailsAfterCommit() {
        final var failingDataSource = dataSourceFailingOn(
                (name, args) -> "setAutoCommit".equals(name) && Boolean.TRUE.equals(args[0]), new ArrayList<>());
        final var userService = txUserService(failingDataSource);
        final var id = lastUserId();

        assertThatCode(() -> userService.changePassword(id, "newPassword", "gugu")).doesNotThrowAnyException();

        assertThat(userDao.findById(id).getPassword()).isEqualTo("newPassword");
        assertThat(TransactionSynchronizationManager.getResource(failingDataSource)).isNull();
    }

    @Test
    void testChangePasswordKeepsResultWhenConnectionCloseFails() {
        final var failingDataSource = dataSourceFailingOn((name, args) -> "close".equals(name), new ArrayList<>());
        final var userService = txUserService(failingDataSource);
        final var id = lastUserId();

        assertThatCode(() -> userService.changePassword(id, "newPassword", "gugu")).doesNotThrowAnyException();

        assertThat(userDao.findById(id).getPassword()).isEqualTo("newPassword");
    }

    @Test
    void testRollbackWhenErrorIsThrown() {
        final var calls = new ArrayList<String>();
        final var dataSource = dataSourceFailingOn((name, args) -> false, calls);
        final var template = new JdbcTemplate(dataSource);
        final var userHistoryDao = new UserHistoryDao(template) {
            @Override
            public void log(final UserHistory userHistory) {
                throw new AssertionError("history failed");
            }
        };
        final var userService = new TxUserService(dataSource, new AppUserService(new UserDao(template), userHistoryDao));
        final var id = lastUserId();
        final var passwordBefore = userDao.findById(id).getPassword();

        assertThrows(AssertionError.class, () -> userService.changePassword(id, "newPassword", "gugu"));

        assertThat(calls).contains("rollback");
        assertThat(userDao.findById(id).getPassword()).isEqualTo(passwordBefore);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    private TxUserService txUserService(final DataSource dataSource) {
        final var template = new JdbcTemplate(dataSource);
        return new TxUserService(dataSource, new AppUserService(new UserDao(template), new UserHistoryDao(template)));
    }

    private long lastUserId() {
        final var users = userDao.findAll();
        return users.get(users.size() - 1).getId();
    }

    private long countHistories() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", (rs, rowNum) -> rs.getLong(1));
    }

    private DataSource dataSourceFailingOn(final BiPredicate<String, Object[]> shouldFail, final List<String> calls) {
        final var delegate = DataSourceConfig.getInstance();
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    final var result = invoke(delegate, method, args);
                    if ("getConnection".equals(method.getName())) {
                        return wrap((Connection) result, shouldFail, calls);
                    }
                    return result;
                });
    }

    private Connection wrap(final Connection connection, final BiPredicate<String, Object[]> shouldFail,
                            final List<String> calls) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    calls.add(method.getName());
                    if ("close".equals(method.getName())) {
                        invoke(connection, method, args);
                    }
                    if (shouldFail.test(method.getName(), args)) {
                        throw new SQLException(method.getName() + " failed");
                    }
                    return "close".equals(method.getName()) ? null : invoke(connection, method, args);
                });
    }

    private Object invoke(final Object target, final java.lang.reflect.Method method, final Object[] args)
            throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
