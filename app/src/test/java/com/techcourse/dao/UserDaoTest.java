package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        connection = dataSource.getConnection();

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao = new UserDao(new JdbcTemplate(dataSource));
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connection, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByWrongAccount() {
        final var account = "gaga";
        assertThatThrownBy(() -> userDao.findByAccount(connection, account))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
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

        userDao.update(connection, user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
