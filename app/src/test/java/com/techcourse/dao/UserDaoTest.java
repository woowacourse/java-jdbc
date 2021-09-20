package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        userDao.removeAll();

        final User user1 = new User("gugu", "password", "hkkang@woowahan.com");
        final User user2 = new User("nabom", "password", "nabom@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).extracting(User::getAccount)
            .containsExactlyInAnyOrder("gugu", "nabom");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findById() {
        final Long userId = userDao.findByAccount("gugu").getId();
        final User foundUser = userDao.findById(userId);

        assertThat(foundUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findByAccount(account);

        assertThat(actual.getAccount()).isNotEmpty();
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
