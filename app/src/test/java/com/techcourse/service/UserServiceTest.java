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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.executeUpdate("truncate table user_history restart identity");
            statement.executeUpdate("truncate table users restart identity");
        }
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void leavesNoTransactionConnectionBound() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
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
        final var history = jdbcTemplate.query(
                "select password from user_history where user_id = ? and created_by = ? and created_at is not null",
                row -> row.getString("password"), 1L, createBy);
        assertThat(history).containsExactly(newPassword);
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

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    void testTransactionRollbackOnSqlFailure() {
        final var userService = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    void testTransactionRollbackOnUncheckedFailure() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                throw new IllegalStateException("History service failed");
            }
        };
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao));

        assertThrows(IllegalStateException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    void savesUserWithoutStartingTransaction() {
        final var userService = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        userService.save(new User("newUser", "password", "new@example.com"));

        assertThat(userDao.findByAccount("newUser").getEmail()).isEqualTo("new@example.com");
    }

    private void assertPasswordAndHistoryUnchanged() {
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) as total from user_history", row -> row.getLong("total"));
        assertThat(historyCount).isZero();
    }
}
