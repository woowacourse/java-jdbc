package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void changePassword_ThenUpdatePasswordAndLogHistory() {
        final var userService = new AppUserService(userDao, new UserHistoryDao(jdbcTemplate));

        userService.changePassword(1L, "qqqqq", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("qqqqq");
        assertThat(historyIdsOf(1L)).hasSize(1);
    }

    @Test
    void save_ThenFindById() {
        final var userService = new AppUserService(userDao, new UserHistoryDao(jdbcTemplate));

        userService.save(new User("roun", "password", "roun@woowahan.com"));

        assertThat(userService.findById(2L).getAccount()).isEqualTo("roun");
    }

    /**
     * 대조군: 트랜잭션 서비스로 감싸지 않으면 실패해도 롤백되지 않는다.
     * 즉 트랜잭션은 AppUserService가 아니라 TxUserService에서만 온다.
     */
    @Test
    void changePassword_WhenFailWithoutTxUserService_ThenNotRollback() {
        final var userService = new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
        assertThat(historyIdsOf(1L)).hasSize(1);
    }

    private List<Long> historyIdsOf(final long userId) {
        return jdbcTemplate.query(
                "select id from user_history where user_id = ?", rs -> rs.getLong("id"), userId);
    }
}
