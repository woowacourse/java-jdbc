package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    private Connection connection;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        try {
            connection = DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(connection, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var user = new User("angie", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
        final var account = "angie";
        final var findUser = userDao.findByAccount(connection, account);

        assertThat(findUser.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(connection, 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L);
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(connection, 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
