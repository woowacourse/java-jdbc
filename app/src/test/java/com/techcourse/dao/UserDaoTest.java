package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.connection.ConnectionManager;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private ConnectionManager connectionManager;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        connectionManager = new ConnectionManager(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connectionManager.getAutoCommittedConnection(), user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connectionManager.getAutoCommittedConnection());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(connectionManager.getAutoCommittedConnection(), 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connectionManager.getAutoCommittedConnection(), account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connectionManager.getAutoCommittedConnection(), user);

        final var actual = userDao.findById(connectionManager.getAutoCommittedConnection(), 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(connectionManager.getAutoCommittedConnection(), 1L);
        user.changePassword(newPassword);

        userDao.update(connectionManager.getAutoCommittedConnection(), user);

        final var actual = userDao.findById(connectionManager.getAutoCommittedConnection(), 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
