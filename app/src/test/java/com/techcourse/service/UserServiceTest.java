package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private long userId;

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        DatabasePopulatorUtils.execute(dataSource);

        final var account = UUID.randomUUID().toString();
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        userId = userDao.findByAccount(account).getId();
    }

    @Test
    void testChangePassword() {
        final var userService = new UserService(DataSourceConfig.getInstance(),
                userDao, new UserHistoryDao(jdbcTemplate));

        final var newPassword = "qqqqq";
        final var createdBy = "gugu";
        userService.changePassword(userId, newPassword, createdBy);

        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        assertThat(userService.findById(userId).getPassword()).isEqualTo(newPassword);
        assertThat(findHistory()).singleElement().satisfies(history -> {
            assertThat(history.password()).isEqualTo(newPassword);
            assertThat(history.createdBy()).isEqualTo(createdBy);
            assertThat(history.createdAt()).isNotNull();
        });
    }

    @Test
    void testTransactionRollback() {
        final var userService = new UserService(DataSourceConfig.getInstance(),
                userDao, new MockUserHistoryDao(jdbcTemplate));
        final var originalPassword = userService.findById(userId).getPassword();

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(userId, "newPassword", "gugu"));

        assertThat(failure).hasMessage("이력 저장 후 발생한 테스트 예외");
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        assertThat(userService.findById(userId).getPassword()).isEqualTo(originalPassword);
        assertThat(findHistory()).isEmpty();
    }

    private List<History> findHistory() {
        return jdbcTemplate.query(
                "select password, created_by, created_at from user_history where user_id = ?",
                rs -> new History(rs.getString("password"), rs.getString("created_by"),
                        rs.getObject("created_at", LocalDateTime.class)),
                userId);
    }

    private record History(String password, String createdBy, LocalDateTime createdAt) {}
}
