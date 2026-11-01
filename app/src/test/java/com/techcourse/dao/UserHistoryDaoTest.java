package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void log() {
        final var account = "history-gugu";
        final var userHistory = new UserHistory(null, 1L, account, "password", "hkkang@woowahan.com", "gugu");

        userHistoryDao.log(userHistory);

        final var sql = "select created_by from user_history where account = ?";
        final var actual = new JdbcTemplate(DataSourceConfig.getInstance())
                .query(sql, rs -> rs.getString("created_by"), account);

        assertThat(actual).containsExactly("gugu");
    }
}
