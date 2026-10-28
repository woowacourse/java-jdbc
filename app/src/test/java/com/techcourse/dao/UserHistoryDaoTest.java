package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
    void log() {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final var userHistory = new UserHistory(user, "admin");

        userHistoryDao.log(userHistory);

        final var actual = jdbcTemplate.queryForObject(
                "select user_id, account, password, email, created_at, created_by from user_history where user_id = ?",
                resultSet -> new SavedUserHistory(
                        resultSet.getLong("user_id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getObject("created_at", LocalDateTime.class),
                        resultSet.getString("created_by")
                ),
                user.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.userId()).isEqualTo(userHistory.getUserId());
        assertThat(actual.account()).isEqualTo(userHistory.getAccount());
        assertThat(actual.password()).isEqualTo(userHistory.getPassword());
        assertThat(actual.email()).isEqualTo(userHistory.getEmail());
        assertThat(actual.createdAt())
                .isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(actual.createdBy()).isEqualTo(userHistory.getCreateBy());
    }

    private record SavedUserHistory(
            long userId,
            String account,
            String password,
            String email,
            LocalDateTime createdAt,
            String createdBy
    ) {
    }
}
