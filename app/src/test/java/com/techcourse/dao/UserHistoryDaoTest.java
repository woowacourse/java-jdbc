package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(dataSource);
    }

    @Test
    @DisplayName("회원 변경 이력을 DB에 저장한다")
    void logSavesHistoryInDatabase() {
        final var user = new User(7L, "db-user", "password", "db-user@example.com");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);

        userHistoryDao.log(new UserHistory(user, "tester"));

        final var userIds = jdbcTemplate.query(
                "select user_id from user_history where created_by = ?",
                resultSet -> resultSet.getLong("user_id"),
                "tester");
        assertThat(userIds).containsExactly(7L);
    }
}
