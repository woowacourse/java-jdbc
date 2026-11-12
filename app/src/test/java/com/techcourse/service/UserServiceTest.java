package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

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
        final var userService = new UserService(DataSourceConfig.getInstance(), userDao, userHistoryDao);

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
        final var userService = new UserService(DataSourceConfig.getInstance(), userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    @Test
    void testTransactionRollbackWhenCommitFails() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSourceFailingOnCommit(), userDao, userHistoryDao);
        final var id = lastUserId();
        final var passwordBefore = userService.findById(id).getPassword();
        final var historyCountBefore = countHistories();

        assertThrows(DataAccessException.class, () -> userService.changePassword(id, "newPassword", "gugu"));

        assertThat(userService.findById(id).getPassword()).isEqualTo(passwordBefore);
        assertThat(countHistories()).isEqualTo(historyCountBefore);
    }

    @Test
    void testChangePasswordWithSingleConnectionPool() {
        final var pool = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "", "");
        pool.setMaxConnections(1);
        pool.setLoginTimeout(1);
        try {
            final var poolTemplate = new JdbcTemplate(pool);
            final var userService = new UserService(pool, new UserDao(poolTemplate), new UserHistoryDao(poolTemplate));
            final var id = lastUserId();

            assertThatCode(() -> userService.changePassword(id, "qqqqq", "gugu")).doesNotThrowAnyException();

            assertThat(userService.findById(id).getPassword()).isEqualTo("qqqqq");
        } finally {
            pool.dispose();
        }
    }

    private long lastUserId() {
        final var users = userDao.findAll();
        return users.get(users.size() - 1).getId();
    }

    private long countHistories() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", (rs, rowNum) -> rs.getLong(1));
    }

    private DataSource dataSourceFailingOnCommit() {
        final var delegate = DataSourceConfig.getInstance();
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    final var result = invoke(delegate, method, args);
                    return "getConnection".equals(method.getName()) ? failingCommit((Connection) result) : result;
                });
    }

    private Connection failingCommit(final Connection connection) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("commit".equals(method.getName())) {
                        throw new SQLException("commit failed");
                    }
                    return invoke(connection, method, args);
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
