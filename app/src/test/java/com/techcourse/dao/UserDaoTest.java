package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        String password = "password";
        String email = "hkkang@woowahan.com";
        final var user = new User(account, password, email);
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertAll(
                () -> assertThat(actual.getAccount()).isEqualTo(account),
                () -> assertThat(actual.getPassword()).isEqualTo(password),
                () -> assertThat(actual.getEmail()).isEqualTo(email)
        );
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertAll(
                () -> assertThat(actual.getPassword()).isEqualTo(newPassword),
                () -> assertThat(actual.getAccount()).isEqualTo("gugu"),
                () -> assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com")
        );
    }
}
