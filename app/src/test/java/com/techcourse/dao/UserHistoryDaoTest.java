package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("delete from user_history");
    }

    @Test
    void logWithDataSource() {
        assertHistoryIsSaved(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    @Test
    void logWithJdbcTemplate() {
        assertHistoryIsSaved(new UserHistoryDao(jdbcTemplate));
    }

    private void assertHistoryIsSaved(UserHistoryDao dao) {
        UserHistory history = new UserHistory(null, 1L, "gugu", "password", "test@example.com", "admin");

        dao.log(history);

        var rows = jdbcTemplate.findAll("select * from user_history", rs -> {
            assertThat(rs.getLong("user_id")).isEqualTo(history.getUserId());
            assertThat(rs.getString("account")).isEqualTo(history.getAccount());
            assertThat(rs.getString("password")).isEqualTo(history.getPassword());
            assertThat(rs.getString("email")).isEqualTo(history.getEmail());
            assertThat(rs.getTimestamp("created_at")).isNotNull();
            assertThat(rs.getString("created_by")).isEqualTo(history.getCreateBy());
            return rs.getLong("id");
        });
        assertThat(rows).hasSize(1);
    }
}
