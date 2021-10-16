package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        userDao.insert(new User("woogie", "password", "woogie@woowahan.com"));
    }

    @AfterEach
    void cleanUp() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    void findById() {
        final List<User> users = userDao.findAll();
        final User expected = userDao.findById(users.get(0).getId());

        assertThat(expected.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        userDao.insert(user);

        final List<User> users = userDao.findAll();
        final User actual = userDao.findById(users.get(2).getId());

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final List<User> users = userDao.findAll();
        final User user = userDao.findById(users.get(0).getId());
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(users.get(0).getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
