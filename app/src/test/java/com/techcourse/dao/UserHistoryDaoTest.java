package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from user_history");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("alter table user_history alter column id restart with 1");
        jdbcTemplate.update("alter table users alter column id restart with 1");
    }

    @Test
    void log() {
        final var user = userDao.findById(1L);
        final var userHistory = new UserHistory(user, "gugu");

        userHistoryDao.log(userHistory);

        final var histories = userHistoryDao.findAllByUserId(user.getId());
        assertThat(histories).hasSize(1);

        final var actual = histories.get(0);
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getUserId()).isEqualTo(user.getId());
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(actual.getCreatedAt())
                .isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(actual.getCreateBy()).isEqualTo("gugu");
    }
}
