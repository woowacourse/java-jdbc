package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");

        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");

        userHistoryDao.log(new UserHistory(user, "gugu"));

        final var actual = jdbcTemplate.queryForObject(
                "select user_id, account, password, email, created_by from user_history",
                (rs, rowNum) -> List.<Object>of(
                        rs.getLong("user_id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("created_by")));
        assertThat(actual).containsExactly(1L, "gugu", "password", "hkkang@woowahan.com", "gugu");
    }
}
