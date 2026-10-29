package com.techcourse.service;

import com.techcourse.config.UserServiceConfig;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.interface21.transaction.support.TransactionTemplate;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private JdbcConnectionPool dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserService userService;

    @BeforeEach
    void setUp() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID(), "", "");
        dataSource.setMaxConnections(1);
        dataSource.setLoginTimeout(1);
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userService = UserServiceConfig.userService(dataSource);
        userService.save(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @AfterEach
    void tearDown() {
        try {
            assertThat(dataSource.getActiveConnections()).isZero();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            dataSource.dispose();
        }
    }

    @Test
    void testChangePassword() {
        final var before = LocalDateTime.now();
        userService.changePassword(1L, "newPassword", "admin");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
        final var histories = jdbcTemplate.query("select * from user_history", (rs, rowNum) -> {
            assertThat(rs.getLong("user_id")).isEqualTo(1L);
            assertThat(rs.getString("account")).isEqualTo("gugu");
            assertThat(rs.getString("password")).isEqualTo("newPassword");
            assertThat(rs.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(rs.getString("created_by")).isEqualTo("admin");
            return rs.getTimestamp("created_at").toLocalDateTime();
        });
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0)).isBetween(before.minusSeconds(1), LocalDateTime.now());
    }

    @Test
    void testTransactionRollback() {
        // created_by의 NOT NULL 제약으로 실제 이력 INSERT를 실패시킨다.
        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0L);
    }

    @Test
    void failedPasswordUpdateDoesNotCreateHistory() {
        assertThatThrownBy(() -> userService.changePassword(1L, "x".repeat(101), "admin"))
                .isInstanceOf(DataAccessException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0L);
    }

    @Test
    void missingUserRollsBackAndReturnsConnection() {
        assertThatThrownBy(() -> userService.changePassword(999L, "newPassword", "admin"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0L);
    }

    @Test
    void connectionCanBeReusedAfterRollback() {
        assertThatThrownBy(() -> userService.changePassword(1L, "failedPassword", null))
                .isInstanceOf(DataAccessException.class);

        userService.changePassword(1L, "successPassword", "admin");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("successPassword");
        assertHistoryCount(1L);
    }

    @Test
    void decoratorParticipatesInExistingTransaction() {
        final var transactionTemplate = new TransactionTemplate(dataSource);
        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            userService.changePassword(1L, "newPassword", "admin");
            throw new IllegalStateException("outer failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0L);
    }

    private void assertHistoryCount(final long expected) {
        final Long count = jdbcTemplate.queryForObject("select count(*) from user_history",
                (rs, rowNum) -> rs.getLong(1));
        assertThat(count).isEqualTo(expected);
    }
}
