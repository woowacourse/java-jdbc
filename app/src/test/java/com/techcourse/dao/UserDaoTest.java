package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {
    private Connection conn;
    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        conn = dataSource.getConnection();
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(conn, "truncate table users restart identity");

        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(conn, user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(conn);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(conn, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(conn, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(conn, user);

        final var actual = userDao.findById(conn, 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(conn, 1L);
        user.changePassword(newPassword);

        userDao.update(conn, user);

        final var actual = userDao.findById(conn,1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
