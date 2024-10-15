package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private static final RowMapper<UserHistory> USER_HISTORY_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("created_by")
    );

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE user_history RESTART IDENTITY");
    }

    @Test
    void log() {
        UserHistory userHistory = new UserHistory(
                1L,
                1L,
                "gooreum_account",
                "password",
                "email@gmail.com",
                "gooreum"
        );

        userHistoryDao.log(userHistory);

        String sql = "select * from user_history;";
        List<UserHistory> userHistories = jdbcTemplate.query(sql, USER_HISTORY_MAPPER);

        assertThat(userHistories).hasSize(1);
    }
}
