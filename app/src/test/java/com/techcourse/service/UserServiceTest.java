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

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

        jdbcTemplate.update("drop all objects");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = createUserService(userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var beforeChange = LocalDateTime.now();
        userService.changePassword(1L, newPassword, createBy);
        final var afterChange = LocalDateTime.now();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var histories = findHistories();
        assertThat(histories).hasSize(1);
        final var history = histories.getFirst();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.createdBy()).isEqualTo(createBy);
        // DB에 저장할 때 생기는 시각 반올림 차이를 허용한다.
        assertThat(history.createdAt()).isBetween(
                beforeChange.minus(1, ChronoUnit.MILLIS), afterChange.plus(1, ChronoUnit.MILLIS));
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = createUserService(userHistoryDao);

        final var originalPassword = userService.findById(1L).getPassword();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 이력을 저장한 뒤 예외를 발생시켜 두 변경이 모두 취소되는지 확인한다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
        assertThat(findHistories()).isEmpty();
    }

    @Test
    void rollsBackWhenHistoryInsertFails() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = createUserService(userHistoryDao);
        final var originalPassword = userService.findById(1L).getPassword();
        final var invalidCreateBy = "g".repeat(101);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", invalidCreateBy))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        assertThat(findHistories()).isEmpty();
    }

    @Test
    void rollbackPreservesPreviouslyCommittedPasswordAndHistory() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = createUserService(userHistoryDao);
        userService.changePassword(1L, "firstPassword", "gugu");
        final var originalPassword = userService.findById(1L).getPassword();
        final var originalHistories = findHistories();
        assertThat(originalHistories).hasSize(1);
        final UserService failingService = createUserService(new MockUserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class,
                () -> failingService.changePassword(1L, "newPassword", "support"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        assertThat(findHistories()).containsExactlyElementsOf(originalHistories);
    }

    private UserService createUserService(final UserHistoryDao userHistoryDao) {
        return new TxUserService(DataSourceConfig.getInstance(), new AppUserService(userDao, userHistoryDao));
    }

    private List<PasswordChangeHistory> findHistories() {
        final var sql = "select user_id, password, created_at, created_by from user_history order by id";
        return jdbcTemplate.query(sql, row -> new PasswordChangeHistory(
                row.getLong("user_id"),
                row.getString("password"),
                row.getObject("created_at", LocalDateTime.class),
                row.getString("created_by")));
    }

    private record PasswordChangeHistory(long userId, String password, LocalDateTime createdAt, String createdBy) {
    }
}
