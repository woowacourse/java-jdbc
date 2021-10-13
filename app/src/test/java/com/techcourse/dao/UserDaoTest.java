package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        userDao.insert(new User("woogie", "password", "woogie@woowahan.com"));
    }

    @AfterEach
    void cleanUp() throws SQLException {
        userDao.deleteAll();
    }

    @Test
    void findAll() throws SQLException {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    void findById() throws SQLException {
        final User user = userDao.findAll().get(0);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findAll().get(2);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        final String newPassword = "password99";
        final User user = userDao.findAll().get(0);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findAll().get(0);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
