package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    private User gugu;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        gugu = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(gugu);
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(DataSourceConfig.getInstance());
    }

    @Test
    void findAll() {
        final var result = userDao.findAll();

        assertThat(result.size()).isOne();
        assertThat(result.get(0).getAccount()).isEqualTo(gugu.getAccount());
    }

    @Test
    void findById() {
        final var result = userDao.findById(1L);

        assertThat(result.getAccount()).isEqualTo(gugu.getAccount());
    }

    @Test
    void findByAccount() {
        final var result = userDao.findByAccount(gugu.getAccount());

        assertThat(result.getAccount()).isEqualTo(gugu.getAccount());
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
