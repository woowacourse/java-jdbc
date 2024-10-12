package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate.update("DELETE FROM user_history");
        jdbcTemplate.update("ALTER TABLE user_history ALTER COLUMN id RESTART WITH 1");

        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    @DisplayName("Log an user history.")
    void log() {
        // given
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final var history = new UserHistory(
                1L,
                user.getId(),
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                "wonny");

        // when
        userHistoryDao.log(history);

        // then
        final var actual = jdbcTemplate.queryForObject(
                "select * from user_history where id = ?",
                userHistoryRowMapper,
                user.getId());

        assertAll(
                () -> assertThat(actual.getUserId()).isEqualTo(user.getId()),
                () -> assertThat(actual.getAccount()).isEqualTo(user.getAccount()),
                () -> assertThat(actual.getPassword()).isEqualTo(user.getPassword()),
                () -> assertThat(actual.getEmail()).isEqualTo(user.getEmail()),
                () -> assertThat(actual.getCreatedBy()).isEqualTo("wonny"));
    }

    private static final RowMapper<UserHistory> userHistoryRowMapper = (resultSet, __) -> new UserHistory(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("created_by")
    );
}
