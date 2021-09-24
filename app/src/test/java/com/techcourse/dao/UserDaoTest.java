package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        userDao.cleanUp();

        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "another";
        final User anotherUser = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(anotherUser);

        final User user = userDao.findByAccount(account).orElseThrow();

        System.out.println(user);
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findByAccount(account).orElseThrow();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
