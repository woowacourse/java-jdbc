package com.techcourse.service;

import com.techcourse.support.TestDataSource;
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
    private javax.sql.DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSource.create();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(historyCount()).isEqualTo(1);
        final var history = jdbcTemplate.queryForObject(
                "select user_id, password, created_by, created_at from user_history",
                (rs, row) -> {
                    assertThat(rs.getLong("user_id")).isEqualTo(1L);
                    assertThat(rs.getString("password")).isEqualTo(newPassword);
                    assertThat(rs.getString("created_by")).isEqualTo(createBy);
                    return rs.getTimestamp("created_at");
                });
        assertThat(history).isNotNull();
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void rollsBackPasswordAndHistoryWhenFailureOccursAfterInsert() {
        final var failure = new IllegalStateException("failure after history insert");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final java.sql.Connection connection,
                            final com.techcourse.domain.UserHistory history) {
                super.log(connection, history);
                throw failure;
            }
        };
        final var service = new UserService(dataSource, userDao, userHistoryDao);

        assertThat(assertThrows(IllegalStateException.class,
                () -> service.changePassword(1L, "newPassword", "gugu"))).isSameAs(failure);

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void rollsBackPasswordWhenHistoryInsertViolatesConstraint() {
        final var service = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "newPassword", null));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", (rs, row) -> rs.getInt(1));
    }
}
