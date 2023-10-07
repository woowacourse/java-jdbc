package com.techcourse.dao;

import static org.assertj.core.api.Assertions.*;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserHistoryDaoTest {

    UserDao userDao;
    UserHistoryDao userHistoryDao;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userDao.insert(new User("glen", "1234", "glen@fiddich.com"));
    }

    @Test
    void insert() {
        // given
        User user = userDao.findByAccount("glen");
        UserHistory history = new UserHistory(user, "glen");

        // when
        userHistoryDao.insert(history);

        // then
        String sql = "select count(*) from user_history where created_by = ?";
        Long count = jdbcTemplate.queryForObject(sql, rs -> rs.getLong(1), history.getCreateBy());
        assertThat(count).isEqualTo(1);
    }
}
