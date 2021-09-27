package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        List<User> users = userDao.findAll();
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        Optional<User> user = userDao.findById(1L);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getId()).isEqualTo(1L);
        assertThat(user.get().getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String account = "gugu";
        Optional<User> user = userDao.findByAccount(account);

        assertThat(user.get().getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        String password = "password";
        String email = "hkkang@woowahan.com";
        User user = new User(account, password, email);

        // when
        userDao.insert(user);

        // then
        Optional<User> findUser = userDao.findById(2L);
        checkSameUserInfo(account, password, email, findUser.get());
    }

    @Test
    void update() {
        // given
        String newPassword = "password99";
        Optional<User> findUser = userDao.findById(1L);

        // when
        findUser.get().changePassword(newPassword);
        userDao.update(findUser.get());

        // then
        Optional<User> actualUser = userDao.findById(1L);
        checkSameUserInfo(findUser.get(), actualUser.get());
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
