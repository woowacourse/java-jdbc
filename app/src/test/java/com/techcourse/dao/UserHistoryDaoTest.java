package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");

        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void findByUserIdPreservesCreatedAt() {
        final var createdAt = LocalDateTime.of(2026, 10, 30, 12, 34, 56);
        final var userHistory = new UserHistory(null, 1L, "gugu", "password", "gugu@example.com", createdAt, "gugu");
        userHistoryDao.log(userHistory);

        final var histories = userHistoryDao.findByUserId(1L);

        assertThat(histories).singleElement()
                .satisfies(history -> assertThat(history.getCreatedAt()).isEqualTo(createdAt));
    }

    @Test
    void log() {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final var userHistory = new UserHistory(user, "gugu");

        userHistoryDao.log(userHistory);

        final var histories = jdbcTemplate.query(
                "select id, user_id, account, password, email, created_by from user_history",
                rs -> new UserHistory(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("created_by")));

        assertThat(histories).hasSize(1);
        final var actual = histories.getFirst();
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(actual.getCreateBy()).isEqualTo("gugu");
    }
}
