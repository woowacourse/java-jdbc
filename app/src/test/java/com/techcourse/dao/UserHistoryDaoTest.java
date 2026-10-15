package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @Test
    void logStoresEveryHistoryField() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        final var userHistory = new UserHistory(null, 1L, "gugu", "password", "gugu@example.com", "admin");

        new UserHistoryDao(jdbcTemplate).log(userHistory);

        final var saved = jdbcTemplate.queryForObject("select * from user_history", rs -> {
            assertThat(rs.getLong("user_id")).isEqualTo(userHistory.getUserId());
            assertThat(rs.getString("account")).isEqualTo(userHistory.getAccount());
            assertThat(rs.getString("password")).isEqualTo(userHistory.getPassword());
            assertThat(rs.getString("email")).isEqualTo(userHistory.getEmail());
            assertThat(rs.getTimestamp("created_at").toLocalDateTime())
                    .isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(rs.getString("created_by")).isEqualTo(userHistory.getCreateBy());
            return rs.getLong("id");
        });
        assertThat(saved).isEqualTo(1L);
    }
}
