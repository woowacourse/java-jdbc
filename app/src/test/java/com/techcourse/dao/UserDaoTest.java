package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoTest {

    private static UserDao userDao;

    @BeforeAll
    static void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        //when
        List<User> users = userDao.findAll();

        //then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        //when
        Optional<User> user = userDao.findById(1L);
        User result = user.get();

        //then
        assertThat(result.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        //given
        String account = "gugu";

        //when
        Optional<User> user = userDao.findByAccount(account);
        User result = user.get();

        //then
        assertThat(result.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        //given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        //when
        userDao.insert(user);
        Optional<User> actual = userDao.findById(2L);
        User result = actual.get();

        //then
        assertThat(result.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        //given
        String newPassword = "password99";
        Optional<User> user = userDao.findById(1L);
        User initialUser = user.get();

        //when
        initialUser.changePassword(newPassword);
        userDao.update(initialUser);

        Optional<User> actual = userDao.findById(1L);
        User changedUser = actual.get();

        //then
        assertThat(changedUser.getPassword()).isEqualTo(newPassword);
    }
}
