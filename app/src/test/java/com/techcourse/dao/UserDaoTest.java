package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        // when
        final List<User> users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // when
        final Optional<User> user = userDao.findById(1L);

        // then
        assertThat(user).isPresent();
    }

    @Test
    void findByAccount() {
        // given
        final String account = "gugu";

        // when
        final Optional<User> user = userDao.findByAccount(account);

        // then
        assertThat(user).isPresent();
    }

    @Test
    void insert() {
        // given
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        final Optional<User> actual = userDao.findById(2L);
        assertThat(actual).isPresent();
    }

    @Test
    void update() {
        // given
        final String newPassword = "password99";
        final User user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        final User actual = userDao.findById(1L).orElseThrow();
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
