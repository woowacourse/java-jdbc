package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private Connection conn;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        conn = DataSourceConfig.getInstance().getConnection();

        userDao = new UserDao(new JdbcTemplate());
        final var user = new User("ever", "password", "ever@woowahan.com");
        userDao.insert(conn, user);
    }

    @Test
    void findAll() {
        userDao.insert(conn, new User("ever2", "password", "ever@woowahan.com"));
        final var users = userDao.findAll(conn);

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
    }

    @Test
    void findById() {
        final var user = userDao.findById(conn, 1L);

        assertThat(user.getAccount()).isEqualTo("ever");
    }

    @Test
    void findByAccount() {
        final var account = "ever";
        final var user = userDao.findByAccount(conn, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-ever";
        final var user = new User(account, "password", "ever@woowahan.com");
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

        final var actual = userDao.findById(conn, 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
