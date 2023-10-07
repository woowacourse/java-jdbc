package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionManager;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(getConnection(), user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(getConnection());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(getConnection(), 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "sunshot";
        userDao.insert(TransactionManager.getConnection(DataSourceConfig.getInstance()),
                new User("sunshot", "password", "sunshot@woowa.com"));
        final var user = userDao.findByAccount(getConnection(), account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(getConnection(), user);

        final var actual = userDao.findById(getConnection(), 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(getConnection(), 1L);
        user.changePassword(newPassword);

        userDao.update(getConnection(), user);

        final var actual = userDao.findById(getConnection(), 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    private Connection getConnection() {
        return TransactionManager.getConnection(DataSourceConfig.getInstance());
    }
}
