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
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @AfterEach
    void clear() {
        jdbcTemplate.update("truncate table user_history restart identity");
    }

    @Test
    void log() {
        final var userHistory = new UserHistory(null, 1L, "gugu", "password", "hkkang@woowahan.com", "gugu");

        userHistoryDao.log(userHistory);

        final var actual = jdbcTemplate.queryForObject(
                "select user_id, account, created_by from user_history where id = ?",
                (rs, rowNum) -> new UserHistory(null, rs.getLong("user_id"), rs.getString("account"), null, null,
                        rs.getString("created_by")),
                1L
        );
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getCreateBy()).isEqualTo("gugu");
    }
}
