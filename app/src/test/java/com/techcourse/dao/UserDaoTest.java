package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

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
        // when
        final var users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // when
        final var user = userDao.findById(1L);

        // then
        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        // given
        final String account = "alien";
        final String password = "password";
        final String email = "alien@woowahan.com";
        userDao.insert(new User(account, password, email));

        // when
        final var user = userDao.findByAccount(account);

        // then
        assertThat(user.getAccount())
                .isEqualTo(account);
    }

    @Test
    void insert() {
        // given
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var actual = userDao.findById(2L);

        // then
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        // given
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
