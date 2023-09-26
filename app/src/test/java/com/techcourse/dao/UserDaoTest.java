package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
    }

    @Test
    void findAll() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        final var user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccount_resultSizeTwo_fail() {
        final var account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        assertThatThrownBy(() -> userDao.findByAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
