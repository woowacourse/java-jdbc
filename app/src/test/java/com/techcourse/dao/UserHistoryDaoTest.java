package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private User user;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);

        userDao = new UserDao(jdbcTemplate) {
        };
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void log() throws SQLException {
        user = userDao.findByAccount("gugu");
        final UserHistory userHistory = new UserHistory(user, user.getAccount());

        userHistoryDao.log(dataSource.getConnection(), userHistory);

        UserHistory actual = userHistoryDao.findById(user.getId());
        assertThat(actual.getUserId()).isEqualTo(user.getId());
    }
}
