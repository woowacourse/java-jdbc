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

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        final var historyPassword = jdbcTemplate.queryForObject("select password from user_history where user_id = ?",
                rs -> rs.getString("password"), 1L);
        assertThat(historyPassword).isEqualTo(newPassword);
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
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
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
        assertThat(historyCount).isZero();
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void rollsBackBothWritesWhenHistoryDaoFailsAfterInsert() {
        final var historyDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory history) {
                super.log(history);
                throw new DataAccessException("Failed after inserting history");
            }
        };
        final var service = new TxUserService(new AppUserService(userDao, historyDao));

        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "newPassword", "admin"));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
        assertThat(historyCount).isZero();
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void historyConstraintViolationRollsBackPasswordChange() {
        final var service = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
        assertThat(historyCount).isZero();
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void canStartAnotherTransactionAfterRollback() {
        final var failingService = new TxUserService(new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate)));
        assertThrows(DataAccessException.class, () -> failingService.changePassword(1L, "failedPassword", "admin"));
        final var service = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        service.changePassword(1L, "newPassword", "admin");

        assertThat(service.findById(1L).getPassword()).isEqualTo("newPassword");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void saveDelegatesToApplicationService() {
        final var service = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        service.save(new User("other", "otherPassword", "other@example.com"));

        assertThat(service.findById(2L).getAccount()).isEqualTo("other");
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }
}
