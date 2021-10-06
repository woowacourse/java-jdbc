package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private static UserDao userDao;

    @BeforeAll
    static void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user1 = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user1);
        final User user2 = new User("joel", "joel", "hkkang@woowahan.com");
        userDao.insert(user2);
        final User user3 = new User("middlebear", "middlebear", "hkkang@woowahan.com");
        userDao.insert(user3);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).hasSizeGreaterThan(3);
        assertThat(users.get(0).getAccount()).isEqualTo("gugu");
        assertThat(users.get(1).getAccount()).isEqualTo("joel");
        assertThat(users.get(2).getAccount()).isEqualTo("middlebear");
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
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

        final User actual = userDao.findById(4L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
