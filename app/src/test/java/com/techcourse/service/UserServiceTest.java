package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.UserHistory;
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
        final var userService = new TxUserService(DataSourceConfig.getInstance(), appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final var historyPassword = jdbcTemplate.queryForObject(
                "select password from user_history where user_id = ?", resultSet -> resultSet.getString("password"), 1L);
        assertThat(historyPassword).isEqualTo(newPassword);
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(DataSourceConfig.getInstance(), appUserService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void 이력_저장_후_실패하면_비밀번호와_이력이_함께_롤백된다() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw new DataAccessException("이력 저장 후 실패");
            }
        };
        final var userService = new TxUserService(DataSourceConfig.getInstance(),
                new AppUserService(userDao, userHistoryDao));

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
    }

    @Test
    void 롤백한_다음에도_같은_스레드에서_새_트랜잭션을_실행할_수_있다() {
        final var failingService = new TxUserService(DataSourceConfig.getInstance(),
                new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate)));
        assertThrows(DataAccessException.class,
                () -> failingService.changePassword(1L, "failedPassword", "gugu"));

        final var userService = new TxUserService(DataSourceConfig.getInstance(),
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));
        userService.changePassword(1L, "nextPassword", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("nextPassword");
    }

    @Test
    void 저장은_애플리케이션_서비스로_위임한다() {
        final var userService = new TxUserService(DataSourceConfig.getInstance(),
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        userService.insert(new User("ludens", "password", "ludens@woowahan.com"));

        assertThat(userDao.findByAccount("ludens").getPassword()).isEqualTo("password");
    }
}
