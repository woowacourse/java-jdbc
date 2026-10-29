package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistory history;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final var userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        history = new UserHistory(userDao.findByAccount("gugu"), "admin");
    }

    @Test
    void savesHistoryWithInjectedTemplate() {
        verifySavedHistory(new UserHistoryDao(jdbcTemplate));
    }

    @Test
    void savesHistoryWithDataSource() {
        verifySavedHistory(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    private void verifySavedHistory(final UserHistoryDao dao) {
        dao.log(history);

        final var rows = jdbcTemplate.query("select * from user_history", rs -> {
            assertThat(rs.getLong("user_id")).isEqualTo(history.getUserId());
            assertThat(rs.getString("account")).isEqualTo(history.getAccount());
            assertThat(rs.getString("password")).isEqualTo(history.getPassword());
            assertThat(rs.getString("email")).isEqualTo(history.getEmail());
            assertThat(rs.getTimestamp("created_at").toLocalDateTime())
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(rs.getString("created_by")).isEqualTo(history.getCreateBy());
            return rs.getLong("id");
        });

        assertThat(rows).hasSize(1);
    }
}
