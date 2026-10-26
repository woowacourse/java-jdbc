package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
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
    void log() {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final var userHistory = new UserHistory(user, "gugu");

        userHistoryDao.log(userHistory);

        final var actual = jdbcTemplate.queryForObject(
                "select account from user_history where user_id = ?",
                resultSet -> resultSet.getString("account"),
                user.getId());

        assertThat(actual).isEqualTo(user.getAccount());
    }
}
