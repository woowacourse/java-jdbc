package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import nextstep.jdbc.JdbcTemplate;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.init(dataSource);

        jdbcTemplate = new JdbcTemplate(dataSource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(dataSource);
    }

    @DisplayName("사용자 로그를 남긴다")
    @Test
    void log() {
        final UserHistory userHistory = new UserHistory(
            1L, 1L, "account", "password", "email", "gugu"
        );
        userHistoryDao.log(userHistory);

        final List<Object> userHistories = jdbcTemplate.query("select * from user_history",
            resultSet -> new UserHistory(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"),
                resultSet.getString("created_by")
            ));

        assertAll(
            () -> assertThat(userHistories).hasSize(1),
            () -> assertThat(userHistories.get(0))
                .usingRecursiveComparison()
                .ignoringFields("createdAt")
                .isEqualTo(userHistory)
        );
    }
}
