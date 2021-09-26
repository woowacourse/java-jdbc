package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.save(user);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L).orElseThrow(NullPointerException::new);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account).orElseThrow(NullPointerException::new);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.save(user);

        final User actual = userDao.findById(2L).orElseThrow(NullPointerException::new);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L).orElseThrow(NullPointerException::new);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L).orElseThrow(NullPointerException::new);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
