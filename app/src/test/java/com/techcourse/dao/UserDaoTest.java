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
        User user = new User("gitchan", "password", "gitchan@naver.com");
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
        User user = userDao.findByAccount("gitchan");
        User findUser = userDao.findById(user.getId());

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void findByAccount() {
        final var account = "gitchan";
        final var findUser = userDao.findByAccount(account);

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void insert() {
        final var account = "insert-gitchan";
        final var user = new User(account, "password", "gitchan@naver.com");
        userDao.insert(user);

        final var actual = userDao.findByAccount(account);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findByAccount("gitchan");
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findByAccount("gitchan");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
