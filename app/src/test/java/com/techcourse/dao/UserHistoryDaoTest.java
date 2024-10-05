package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private User user;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() {
        String createdBy = "wiib";

        assertThatCode(() -> userHistoryDao.log(new UserHistory(user, createdBy))).doesNotThrowAnyException();
    }
}
