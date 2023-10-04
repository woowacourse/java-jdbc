package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @Test
    void log() {
        final User philip = new User(1L, "philip", "jaepil", "pilyang.dev@gmail.com");
        final UserHistory userHistory = new UserHistory(philip, "sun-shot");

        userHistoryDao.log(userHistory);

        var selectSql = "select id, user_id, account, password, email, created_at, created_by from user_history where created_by = ?";
        UserHistory result = jdbcTemplate.queryForObject(selectSql,
                resultSet -> new UserHistory(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getString("created_by")
                ),
                "sun-shot");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getAccount()).isEqualTo("philip");
            softly.assertThat(result.getCreateBy()).isEqualTo("sun-shot");
        });
    }

}
