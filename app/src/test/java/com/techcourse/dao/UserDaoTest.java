package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    void findAll() throws SQLException {
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var users = userDao.findAll();
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var newUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(newUser);

        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var newUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(newUser);

        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var newUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(newUser);

        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);
        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var newUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(newUser);

        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
