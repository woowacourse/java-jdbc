package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource;
    private Connection con;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        dataSource = DataSourceConfig.getInstance();
        con = dataSource.getConnection();
        userDao = new UserDao(new JdbcTemplate());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(con, user);
    }

    @AfterEach
    void close() throws SQLException {
        con.close();
    }

    @Test
    void findAll() throws SQLException {
        Connection con = dataSource.getConnection();
        final var users = userDao.findAll(con);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        Connection con = dataSource.getConnection();

        final var user = userDao.findById(con, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        Connection con = dataSource.getConnection();

        final var account = "gugu";
        final var user = userDao.findByAccount(con, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();

        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(con1, user);

        final var actual = userDao.findById(con2, 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        Connection con3 = dataSource.getConnection();

        final var newPassword = "password99";
        final var user = userDao.findById(con1, 1L);
        user.changePassword(newPassword);

        userDao.update(con2, user);

        final var actual = userDao.findById(con3, 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
