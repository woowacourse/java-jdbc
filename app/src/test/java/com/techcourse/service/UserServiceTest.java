package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws java.sql.SQLException {
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop all objects");
        }
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao);

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
        final var userService = new UserService(userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
    @Test
    void commitsPasswordAndHistoryTogether() {
        final var service = new UserService(userDao, new UserHistoryDao(jdbcTemplate));
        service.changePassword(1L, "changed", "operator");
        final var histories = jdbcTemplate.query(
                "select password, created_by from user_history where user_id = ?",
                (rs, row) -> rs.getString("password") + ":" + rs.getString("created_by"), 1L);
        assertThat(histories).containsExactly("changed:operator");
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("changed");
    }

    @Test
    void rollsBackEvenWhenHistoryWasAlreadyInserted() {
        final var historyDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final java.sql.Connection connection,
                            final com.techcourse.domain.UserHistory history) {
                super.log(connection, history);
                throw new DataAccessException("failure after history insert");
            }
        };
        final var service = new UserService(userDao, historyDao);
        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "changed", "operator"));
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", (rs, row) -> rs.getLong(1))).isEmpty();
        // 실패 이후 다음 요청이 새 연결에서 정상 처리되는지 확인한다.
        new UserService(userDao, new UserHistoryDao(jdbcTemplate)).changePassword(1L, "retry", "operator");
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("retry");
    }

}
