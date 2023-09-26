package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
//        userHistoryDao.insert(user);
    }

    @Test
    void log() {
        // given
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final var userHistory = new UserHistory(user, "gugu");

        // when
        userHistoryDao.log(userHistory);

        // then
//        assertThat(actual.getAccount()).isEqualTo(account);
    }
}
