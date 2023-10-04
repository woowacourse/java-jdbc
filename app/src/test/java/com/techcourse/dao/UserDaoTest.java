package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gitchan", "password", "gitchan@naver.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertAll(
                () -> assertThat(users).isNotEmpty(),
                () -> assertThat(users.size()).isEqualTo(1)
        );
    }

    @Test
    void findById() {
        final var findUser = userDao.findById(1L);

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void findByAccount() {
        final String account = "gitchan";
        final User findUser = userDao.findByAccount(account);

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void insert() {
        final var account = "insert-gitchan";
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
