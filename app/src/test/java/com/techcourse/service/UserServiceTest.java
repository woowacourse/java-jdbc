package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:user_service_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
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
        final var histories = jdbcTemplate.query(
                "select password from user_history where user_id = ? and created_by = ? and created_at is not null",
                rs -> rs.getString("password"), 1L, createBy);
        assertThat(histories).containsExactly(newPassword);
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
        assertNoHistory();
    }

    @Test
    void rollsBackPasswordAndHistoryWhenAnExceptionOccursAfterSavingHistory() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory userHistory) {
                super.log(connection, userHistory);
                throw new IllegalStateException("이력 저장 후 실패");
            }
        };
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThrows(IllegalStateException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    @Test
    void rollsBackOnSqlFailureAndCanStartAnotherTransaction() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        // created_by의 NOT NULL 제약 조건 위반으로 실제 SQL 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "failedPassword", null));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();

        userService.changePassword(1L, "newPassword", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isEqualTo(1L);
    }

    @Test
    void doesNotSaveHistoryWhenUserDoesNotExist() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        assertThrows(IllegalStateException.class,
                () -> userService.changePassword(99L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertNoHistory();
    }

    private void assertNoHistory() {
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isZero();
    }
}
