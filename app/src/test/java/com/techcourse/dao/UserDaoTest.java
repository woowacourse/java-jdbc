package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private static final AtomicLong userId = new AtomicLong(0);

    private UserDao userDao;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        user1 = new User(userId.incrementAndGet(), "gugu", "password", "hkkang@woowahan.com");
        user2 = new User(userId.incrementAndGet(), "solong", "password", "solong@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);
    }

    @AfterEach
    void afterAll() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getAccount()).isEqualTo(user1.getAccount());
        assertThat(users.get(1).getAccount()).isEqualTo(user2.getAccount());
    }

    @Test
    void findById() {
        final User user = userDao.findById(user1.getId());

        assertThat(user.getAccount()).isEqualTo(user1.getAccount());
    }

    @Test
    void findByAccount() {
        final User user = userDao.findByAccount(user1.getAccount());

        assertThat(user.getAccount()).isEqualTo(user1.getAccount());
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findById(userId.incrementAndGet());

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(user1.getId());
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(user1.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
