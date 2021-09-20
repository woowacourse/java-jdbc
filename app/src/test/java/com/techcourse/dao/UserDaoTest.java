package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
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
        // given
        final String account = "insert-gugu";
        final String password = "password";
        final String email = "hkkang@woowahan.com";
        final User user = new User(account, password, email);

        // when
        userDao.insert(user);

        // then
        final User actual = userDao.findById(2L);
        checkSameUserInfo(account, password, email, actual);
    }

    @Test
    void update() {
        // given
        final String newPassword = "password99";
        final User user = userDao.findById(1L);

        // when
        user.changePassword(newPassword);
        userDao.update(user);

        // then
        final User actual = userDao.findById(1L);
        checkSameUserInfo(user, actual);
    }

    private void checkSameUserInfo(final User user, final User actual) {
        assertThat(user).usingRecursiveComparison().isEqualTo(actual);
    }

    private void checkSameUserInfo(final String account, final String password, final String email, final User actual) {
        assertThat(actual.getId()).isEqualTo(2L);
        assertThat(actual.getAccount()).isEqualTo(account);
        assertThat(actual.getPassword()).isEqualTo(password);
        assertThat(actual.getEmail()).isEqualTo(email);
    }
}
