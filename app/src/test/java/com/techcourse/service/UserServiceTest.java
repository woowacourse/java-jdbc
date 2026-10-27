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


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var historyPasswords = jdbcTemplate.query(
                "select password from user_history where user_id = ? and created_by = ?",
                rs -> rs.getString("password"), 1L, createBy);

        assertThat(historyPasswords).containsExactly(newPassword);
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

        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ?",
                rs -> rs.getLong(1), 1L);
        assertThat(historyCount).isZero();
    }

    @Test
    void rollsBackPasswordAndHistoryWhenFailureOccursAfterHistoryInsert() {
        final var exception = new DataAccessException("이력 저장 후 실패");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw exception;
            }
        };
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var actualException = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actualException).isSameAs(exception);
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ?",
                rs -> rs.getLong(1), 1L);
        assertThat(historyCount).isZero();
    }
}
