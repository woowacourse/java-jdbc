package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        userDao = new UserDao(jdbcTemplate);
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String account = "gugu";
        User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        String newPassword = "password99";
        User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void updateWithConnection() throws SQLException {
        String newPassword = "password99";
        User user = userDao.findById(1L);
        user.changePassword(newPassword);
        Connection connection = DataSourceConfig.getInstance().getConnection();

        userDao.update(connection, user);

        User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
