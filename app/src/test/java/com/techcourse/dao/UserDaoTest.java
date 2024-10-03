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
    private User preSavedUser;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
        User gugu = new User("gugu", "password", "hkkang@woowahan.com");

        preSavedUser = userDao.insert(gugu);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var found = userDao.findById(preSavedUser.getId());

        assertThat(found.getAccount()).isEqualTo(preSavedUser.getAccount());
    }

    @Test
    void findByAccount() {
        final var found = userDao.findByAccount(preSavedUser.getAccount());

        assertThat(found.getAccount()).isEqualTo(preSavedUser.getAccount());
    }

    @Test
    void insert() {
        User libi = new User("libi", "password", "libi@test.com");
        User saved = userDao.insert(libi);
        final var actual = userDao.findById(saved.getId());

        assertThat(actual.getAccount()).isEqualTo(libi.getAccount());
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(preSavedUser.getId());
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(preSavedUser.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
