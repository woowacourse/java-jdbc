package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE user_history RESTART IDENTITY");
    }

    @Test
    void UserHistory_객체를_저장한다() {
        // given
        UserHistory userHistory = new UserHistory(1L, 1L, "prin", "1q2w3e4r!@", "prin@gmail.com", "admin");

        // when
        userHistoryDao.log(userHistory);

        // then
        long count = jdbcTemplate.queryForObject("SELECT count(*) FROM user_history", rs -> rs.getLong(1)).orElseThrow();
        assertThat(count).isEqualTo(1);
    }
}
