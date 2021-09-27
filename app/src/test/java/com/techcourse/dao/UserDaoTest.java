package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        final User user = new User("gugu1", "password1", "hkkang@woowahan.com1");
        userDao.insert(user);

        assertThat(userDao.findAll()).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByIdThrowNotExists() {
        assertThatThrownBy(()  -> userDao.findById(20L)).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void findByIdThrowIncorrectSize() {
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        assertThatThrownBy(()  -> userDao.findByAccount("gugu")).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void insert() {
        List<User> before = userDao.findAll();

        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        List<User> actual = userDao.findAll();

        assertThat(actual.size() - before.size()).isEqualTo(1);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
