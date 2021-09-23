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
        //given
        //when
        final List<User> users = userDao.findAll();
        //then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        //given
        //when
        final User user = userDao.findById(1L);
        //then
        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        //given
        final String account = "gugu";
        //when
        final User user = userDao.findByAccount(account);
        //then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        //given
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        //when
        userDao.insert(user);
        final User actual = userDao.findById(2L);
        //then
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        //given
        final String newPassword = "password99";
        //when
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);
        userDao.update(user);
        final User actual = userDao.findById(1L);
        //then
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void deleteAll() {
        //given
        //when
        userDao.deleteAll();
        final List<User> users = userDao.findAll();
        //then
        assertThat(users).isEmpty();
    }
}
