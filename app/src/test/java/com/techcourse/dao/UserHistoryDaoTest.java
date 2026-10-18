package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private static final RowMapper<SavedHistory> SAVED_HISTORY_ROW_MAPPER = (rs, rowNum) -> new SavedHistory(
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getString("created_by"));

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
    void log() {
        final var userHistory = new UserHistory(null, 1L, "gugu", "password", "hkkang@woowahan.com", "admin");

        userHistoryDao.log(userHistory);

        final var saved = jdbcTemplate.queryForOptional(
                "select user_id, account, password, email, created_at, created_by from user_history",
                SAVED_HISTORY_ROW_MAPPER).orElseThrow();
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(new SavedHistory(1L, "gugu", "password", "hkkang@woowahan.com", null, "admin"));
        assertThat(saved.createdAt()).isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.SECONDS));
    }

    private record SavedHistory(
            long userId,
            String account,
            String password,
            String email,
            LocalDateTime createdAt,
            String createdBy
    ) {
    }
}
